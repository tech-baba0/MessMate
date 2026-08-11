package com.messmate.backend.service;

import com.messmate.backend.entity.MemberSettlementSummary;
import com.messmate.backend.entity.MonthlySettlement;
import com.messmate.backend.entity.User;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.repository.MonthlySettlementRepository;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SettlementService {

    @Autowired
    private MonthlySettlementRepository monthlySettlementRepository;

    @Autowired
    private MessMemberRepository messMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceService balanceService;

    @Transactional
    public MonthlySettlement generateSettlement(String messId, String monthYear) {
        Optional<MonthlySettlement> existingOpt = monthlySettlementRepository.findByMessIdAndMonthYear(messId,
                monthYear);
        if (existingOpt.isPresent() && "CLOSED".equals(existingOpt.get().getStatus())) {
            throw new RuntimeException("Settlement is already closed for " + monthYear);
        }

        List<MessMember> members = messMemberRepository.findByMessId(messId);
        List<MemberSettlementSummary> summaries = new ArrayList<>();

        double totalExpenses = 0.0;
        double totalMeals = 0.0;

        for (MessMember member : members) {
            String userId = member.getUserId();
            User user = userRepository.findById(userId).orElse(null);
            if (user == null)
                continue;

            var balance = balanceService.getBalanceForUser(messId, userId);

            MemberSettlementSummary summary = MemberSettlementSummary.builder()
                    .userId(userId)
                    .name(user.getName())
                    .mealsTaken(0.0) // Place-holder for precise date filtering
                    .expenseShare(balance.getTotalExpenseShare())
                    .paidAmount(balance.getTotalPaidForBazar() + balance.getPaymentsMade())
                    .netBalance(balance.getNetBalance())
                    .build();

            summaries.add(summary);
            totalExpenses += balance.getTotalExpenseShare();
        }

        MonthlySettlement settlement;
        if (existingOpt.isPresent()) {
            settlement = existingOpt.get();
            settlement.setMemberSummaries(summaries);
            settlement.setGeneratedAt(LocalDate.now());
            settlement.setTotalExpenses(totalExpenses);
        } else {
            settlement = MonthlySettlement.builder()
                    .messId(messId)
                    .monthYear(monthYear)
                    .status("GENERATED")
                    .generatedAt(LocalDate.now())
                    .memberSummaries(summaries)
                    .totalExpenses(totalExpenses)
                    .totalMeals(totalMeals)
                    .build();
        }

        return monthlySettlementRepository.save(settlement);
    }

    public MonthlySettlement closeSettlement(String messId, String monthYear) {
        MonthlySettlement settlement = monthlySettlementRepository.findByMessIdAndMonthYear(messId, monthYear)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));

        settlement.setStatus("CLOSED");
        settlement.setClosedAt(LocalDate.now());
        return monthlySettlementRepository.save(settlement);
    }

    public MonthlySettlement reopenSettlement(String messId, String monthYear) {
        MonthlySettlement settlement = monthlySettlementRepository.findByMessIdAndMonthYear(messId, monthYear)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));

        settlement.setStatus("GENERATED");
        settlement.setClosedAt(null);
        return monthlySettlementRepository.save(settlement);
    }
}
