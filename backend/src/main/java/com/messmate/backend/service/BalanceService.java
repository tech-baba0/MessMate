package com.messmate.backend.service;

import com.messmate.backend.dto.response.BalanceResponse;
import com.messmate.backend.entity.Expense;
import com.messmate.backend.entity.ExpenseShare;
import com.messmate.backend.entity.Payment;
import com.messmate.backend.entity.User;
import com.messmate.backend.repository.ExpenseRepository;
import com.messmate.backend.repository.ExpenseShareRepository;
import com.messmate.backend.repository.PaymentRepository;
import com.messmate.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BalanceService {

        @Autowired
        private ExpenseRepository expenseRepository;

        @Autowired
        private ExpenseShareRepository expenseShareRepository;

        @Autowired
        private PaymentRepository paymentRepository;

        @Autowired
        private UserRepository userRepository;

        public BalanceResponse getBalanceForUser(String messId, String userId) {
                User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

                List<Expense> allExpenses = expenseRepository.findByMessId(messId).stream()
                                .filter(e -> "ACTIVE".equals(e.getStatus()))
                                .collect(Collectors.toList());

                double totalPaidForBazar = allExpenses.stream()
                                .filter(e -> userId.equals(e.getPurchasedById()))
                                .mapToDouble(Expense::getTotalAmount)
                                .sum();

                List<ExpenseShare> myShares = expenseShareRepository.findByMessIdAndUserId(messId, userId);
                double totalExpenseShare = myShares.stream()
                                .mapToDouble(ExpenseShare::getShareAmount)
                                .sum();

                List<Payment> allPayments = paymentRepository.findByMessId(messId).stream()
                                .filter(p -> "COMPLETED".equals(p.getStatus()))
                                .collect(Collectors.toList());

                double paymentsMade = allPayments.stream()
                                .filter(p -> userId.equals(p.getPaidById()))
                                .mapToDouble(Payment::getAmount)
                                .sum();

                double pendingPaymentsMade = paymentRepository.findByMessId(messId).stream()
                                .filter(p -> "PENDING".equals(p.getStatus()))
                                .filter(p -> userId.equals(p.getPaidById()))
                                .mapToDouble(Payment::getAmount)
                                .sum();

                double paymentsReceived = allPayments.stream()
                                .filter(p -> userId.equals(p.getPaidToId()))
                                .mapToDouble(Payment::getAmount)
                                .sum();

                double totalGiven = totalPaidForBazar + paymentsMade;
                double totalConsumed = totalExpenseShare + paymentsReceived;

                double netBalance = totalGiven - totalConsumed;

                String balanceMessage;
                if (Math.abs(netBalance) < 0.01) {
                        balanceMessage = "Settled";
                } else if (netBalance > 0) {
                        balanceMessage = "You should receive ₹" + String.format("%.2f", netBalance);
                } else {
                        balanceMessage = "You owe ₹" + String.format("%.2f", Math.abs(netBalance));
                }

                return BalanceResponse.builder()
                                .userId(userId)
                                .name(user.getName())
                                .totalExpenseShare(totalExpenseShare)
                                .totalPaidForBazar(totalPaidForBazar)
                                .paymentsMade(paymentsMade)
                                .pendingPaymentsMade(pendingPaymentsMade)
                                .paymentsReceived(paymentsReceived)
                                .netBalance(netBalance)
                                .balanceMessage(balanceMessage)
                                .build();
        }

        @Autowired
        private com.messmate.backend.repository.MessMemberRepository messMemberRepository;

        public com.messmate.backend.dto.response.GroupBalanceResponse getGroupBalances(String messId) {
                // Get all active/approved members
                List<com.messmate.backend.entity.MessMember> members = messMemberRepository.findByMessId(messId)
                                .stream()
                                .filter(m -> "ACTIVE".equals(m.getStatus()) || "APPROVED".equals(m.getStatus()))
                                .collect(Collectors.toList());

                List<BalanceResponse> allBalances = new java.util.ArrayList<>();
                for (com.messmate.backend.entity.MessMember m : members) {
                        try {
                                allBalances.add(getBalanceForUser(messId, m.getUserId()));
                        } catch (Exception e) {
                        }
                }

                // Calculate settlements
                List<BalanceResponse> debtors = allBalances.stream()
                                .filter(b -> b.getNetBalance() < -0.01)
                                .sorted(java.util.Comparator.comparing(BalanceResponse::getNetBalance)) // lowest first
                                                                                                        // (most in
                                                                                                        // debt)
                                .collect(Collectors.toList());

                List<BalanceResponse> creditors = allBalances.stream()
                                .filter(b -> b.getNetBalance() > 0.01)
                                .sorted((b1, b2) -> Double.compare(b2.getNetBalance(), b1.getNetBalance())) // highest
                                                                                                            // first
                                                                                                            // (most
                                                                                                            // owed)
                                .collect(Collectors.toList());

                List<com.messmate.backend.dto.response.SuggestedReimbursement> reimbursements = new java.util.ArrayList<>();

                int dIndex = 0;
                int cIndex = 0;

                // Working copies to mutate while settling
                double[] debtAmts = debtors.stream().mapToDouble(b -> Math.abs(b.getNetBalance())).toArray();
                double[] creditAmts = creditors.stream().mapToDouble(BalanceResponse::getNetBalance).toArray();

                while (dIndex < debtors.size() && cIndex < creditors.size()) {
                        double debt = debtAmts[dIndex];
                        double credit = creditAmts[cIndex];

                        if (debt < 0.01) {
                                dIndex++;
                                continue;
                        }
                        if (credit < 0.01) {
                                cIndex++;
                                continue;
                        }

                        double amountSettled = Math.min(debt, credit);

                        BalanceResponse debtor = debtors.get(dIndex);
                        BalanceResponse creditor = creditors.get(cIndex);

                        reimbursements.add(com.messmate.backend.dto.response.SuggestedReimbursement.builder()
                                        .fromUserId(debtor.getUserId())
                                        .fromUserName(debtor.getName())
                                        .toUserId(creditor.getUserId())
                                        .toUserName(creditor.getName())
                                        .amount(Math.round(amountSettled * 100.0) / 100.0)
                                        .build());

                        debtAmts[dIndex] -= amountSettled;
                        creditAmts[cIndex] -= amountSettled;

                        if (debtAmts[dIndex] < 0.01)
                                dIndex++;
                        if (creditAmts[cIndex] < 0.01)
                                cIndex++;
                }

                // Sort balances: Highest owed first, then highest debt
                allBalances.sort((b1, b2) -> Double.compare(b2.getNetBalance(), b1.getNetBalance()));

                return com.messmate.backend.dto.response.GroupBalanceResponse.builder()
                                .userBalances(allBalances)
                                .suggestedReimbursements(reimbursements)
                                .build();
        }
}
