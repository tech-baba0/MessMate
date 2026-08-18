package com.messmate.backend.service;

import com.messmate.backend.dto.request.ExpenseRequest;
import com.messmate.backend.entity.Expense;
import com.messmate.backend.entity.ExpenseShare;
import com.messmate.backend.entity.MealEntry;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.repository.ExpenseRepository;
import com.messmate.backend.repository.ExpenseShareRepository;
import com.messmate.backend.repository.MealRepository;
import com.messmate.backend.repository.MessMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private MessMemberRepository messMemberRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private FcmService fcmService;

    @Autowired
    private com.messmate.backend.repository.UserRepository userRepository;

    @Transactional
    public Expense createExpense(String messId, String userId, ExpenseRequest request) {
        Expense expense = Expense.builder()
                .messId(messId)
                .purchasedById(
                        request.getPaidBy() != null && !request.getPaidBy().isEmpty() ? request.getPaidBy() : userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .totalAmount(request.getTotalAmount())
                .splitMethod(request.getSplitMethod())
                .category(request.getCategory())
                .mealScope(request.getMealScope())
                .receiptUrl(request.getReceiptUrl())
                .status("ACTIVE")
                .items(request.getItems())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        List<ExpenseShare> newShares = previewSplit(messId, request);
        for (ExpenseShare s : newShares) {
            s.setExpenseId(savedExpense.getId());
            expenseShareRepository.save(s);
        }

        // Notify all members
        try {
            List<MessMember> members = messMemberRepository.findByMessId(messId);
            for (MessMember m : members) {
                if (!m.getUserId().equals(userId)) {
                    userRepository.findById(m.getUserId()).ifPresent(user -> {
                        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                            java.util.Map<String, String> data = new java.util.HashMap<>();
                            data.put("type", "EXPENSE_ADDED");
                            fcmService.sendPushNotificationWithData(
                                    user.getFcmToken(),
                                    "New Bazar Expense",
                                    "An expense '" + request.getTitle() + "' was just added.",
                                    data);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedExpense;
    }

    public List<Expense> getExpenses(String messId) {
        return expenseRepository.findByMessId(messId);
    }

    @Transactional
    public Expense updateExpense(String messId, String expenseId, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getMessId().equals(messId)) {
            throw new RuntimeException("Expense does not belong to this mess");
        }
        if ("CANCELLED".equals(expense.getStatus())) {
            throw new RuntimeException("Cannot edit a cancelled expense");
        }

        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setTotalAmount(request.getTotalAmount());
        expense.setSplitMethod(request.getSplitMethod());
        expense.setCategory(request.getCategory());
        expense.setMealScope(request.getMealScope());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense.setItems(request.getItems());

        if (request.getPaidBy() != null && !request.getPaidBy().isEmpty()) {
            expense.setPurchasedById(request.getPaidBy());
        }

        // Remove old shares and recalculate
        List<ExpenseShare> oldShares = expenseShareRepository.findByExpenseId(expenseId);
        expenseShareRepository.deleteAll(oldShares);

        Expense updatedExpense = expenseRepository.save(expense);

        List<ExpenseShare> newShares = previewSplit(messId, request);
        for (ExpenseShare s : newShares) {
            s.setExpenseId(updatedExpense.getId());
            expenseShareRepository.save(s);
        }

        return updatedExpense;
    }

    @Transactional
    public void cancelExpense(String messId, String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getMessId().equals(messId)) {
            throw new RuntimeException("Expense does not belong to this mess");
        }

        expense.setStatus("CANCELLED");
        expenseRepository.save(expense);

        List<ExpenseShare> oldShares = expenseShareRepository.findByExpenseId(expenseId);
        expenseShareRepository.deleteAll(oldShares);
    }

    public List<ExpenseShare> previewSplit(String messId, ExpenseRequest request) {
        List<MessMember> members = messMemberRepository.findByMessId(messId)
                .stream()
                .filter(m -> "ACTIVE".equals(m.getStatus()) || "APPROVED".equals(m.getStatus()))
                .collect(Collectors.toList());

        if (members.isEmpty())
            return new java.util.ArrayList<>();

        List<ExpenseShare> shares = new java.util.ArrayList<>();
        String method = request.getSplitMethod();
        if (method == null)
            method = "AUTO_MEAL";

        if ("EQUAL".equalsIgnoreCase(method)) {
            double share = request.getTotalAmount() / members.size();
            for (MessMember member : members) {
                shares.add(ExpenseShare.builder().messId(messId).userId(member.getUserId()).shareAmount(share).build());
            }
        } else if ("CUSTOM_PERCENTAGE".equalsIgnoreCase(method) || "CUSTOM_FIXED".equalsIgnoreCase(method)) {
            if (request.getCustomSplits() != null) {
                for (com.messmate.backend.dto.request.CustomSplit cs : request.getCustomSplits()) {
                    double amt = "CUSTOM_PERCENTAGE".equalsIgnoreCase(method)
                            ? (request.getTotalAmount() * (cs.getPercentage() / 100.0))
                            : cs.getAmount();
                    shares.add(ExpenseShare.builder().messId(messId).userId(cs.getMemberId()).shareAmount(amt).build());
                }
            }
        } else {
            // AUTO_MEAL
            List<MealEntry> dailyMeals = mealRepository.findByMessIdAndDate(messId, request.getDate());

            String scope = request.getMealScope() != null ? request.getMealScope() : "BOTH";

            double totalUnits = 0.0;
            for (MealEntry meal : dailyMeals) {
                double u = 0.0;
                if (meal.getLunch() != null && meal.getLunch() && ("LUNCH".equals(scope) || "BOTH".equals(scope)))
                    u += 1.0;
                if (meal.getDinner() != null && meal.getDinner() && ("DINNER".equals(scope) || "BOTH".equals(scope)))
                    u += 1.0;
                totalUnits += u;
            }

            if (totalUnits > 0) {
                double ratePerUnit = request.getTotalAmount() / totalUnits;
                for (MealEntry meal : dailyMeals) {
                    double u = 0.0;
                    if (meal.getLunch() != null && meal.getLunch() && ("LUNCH".equals(scope) || "BOTH".equals(scope)))
                        u += 1.0;
                    if (meal.getDinner() != null && meal.getDinner()
                            && ("DINNER".equals(scope) || "BOTH".equals(scope)))
                        u += 1.0;
                    if (u > 0) {
                        shares.add(ExpenseShare.builder().messId(messId).userId(meal.getUserId())
                                .shareAmount(u * ratePerUnit).build());
                    }
                }
            } else {
                // Fallback if nobody ate
                double share = request.getTotalAmount() / members.size();
                for (MessMember member : members) {
                    shares.add(ExpenseShare.builder().messId(messId).userId(member.getUserId()).shareAmount(share)
                            .build());
                }
            }
        }
        return shares;
    }
}
