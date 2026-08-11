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
                .paymentsReceived(paymentsReceived)
                .netBalance(netBalance)
                .balanceMessage(balanceMessage)
                .build();
    }
}
