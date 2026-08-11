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

    @Transactional
    public Expense createExpense(String messId, String userId, ExpenseRequest request) {
        Expense expense = Expense.builder()
                .messId(messId)
                .purchasedById(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .totalAmount(request.getTotalAmount())
                .splitMethod(request.getSplitMethod())
                .status("ACTIVE")
                .items(request.getItems())
                .build();
                
        Expense savedExpense = expenseRepository.save(expense);
        splitExpense(savedExpense);
        
        return savedExpense;
    }
    
    private void splitExpense(Expense expense) {
        List<MessMember> members = messMemberRepository.findByMessId(expense.getMessId())
                .stream()
                .filter(m -> m.getStatus().equals("APPROVED"))
                .collect(Collectors.toList());
                
        if (members.isEmpty()) return;
        
        if ("EQUAL".equalsIgnoreCase(expense.getSplitMethod())) {
            double share = expense.getTotalAmount() / members.size();
            for (MessMember member : members) {
                ExpenseShare es = ExpenseShare.builder()
                        .expenseId(expense.getId())
                        .messId(expense.getMessId())
                        .userId(member.getUserId())
                        .shareAmount(share)
                        .build();
                expenseShareRepository.save(es);
            }
        } else {
            List<MealEntry> dailyMeals = mealRepository.findByMessIdAndDate(expense.getMessId(), expense.getDate());
            
            double totalUnits = dailyMeals.stream()
                    .mapToDouble(m -> m.getMealUnits() != null ? m.getMealUnits() : 0.0)
                    .sum();
                    
            if (totalUnits > 0) {
                double ratePerUnit = expense.getTotalAmount() / totalUnits;
                
                for (MealEntry meal : dailyMeals) {
                    double units = meal.getMealUnits() != null ? meal.getMealUnits() : 0.0;
                    if (units > 0) {
                        ExpenseShare es = ExpenseShare.builder()
                                .expenseId(expense.getId())
                                .messId(expense.getMessId())
                                .userId(meal.getUserId())
                                .shareAmount(units * ratePerUnit)
                                .build();
                        expenseShareRepository.save(es);
                    }
                }
            } else {
                double share = expense.getTotalAmount() / members.size();
                for (MessMember member : members) {
                    ExpenseShare es = ExpenseShare.builder()
                            .expenseId(expense.getId())
                            .messId(expense.getMessId())
                            .userId(member.getUserId())
                            .shareAmount(share)
                            .build();
                    expenseShareRepository.save(es);
                }
            }
        }
    }
}
