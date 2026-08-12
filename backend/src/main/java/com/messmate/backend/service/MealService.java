package com.messmate.backend.service;

import com.messmate.backend.dto.request.MealToggleRequest;
import com.messmate.backend.dto.response.AdminMealDashboardResponse;
import com.messmate.backend.dto.response.MealHistorySummaryResponse;
import com.messmate.backend.dto.response.MealStatusResponse;
import com.messmate.backend.entity.MealEntry;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.repository.MealRepository;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.repository.MessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private MessRepository messRepository;

    @Autowired
    private MessMemberRepository messMemberRepository;

    public MealEntry toggleMeal(String messId, String userId, MealToggleRequest request) {
        Mess mess = messRepository.findById(messId)
                .orElseThrow(() -> new RuntimeException("Mess not found"));

        MessMember member = messMemberRepository.findByMessIdAndUserId(messId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this mess"));

        boolean isAdmin = (member.getRole() == com.messmate.backend.entity.Role.ROLE_ADMIN);

        java.time.ZoneId indiaZone = java.time.ZoneId.of("Asia/Kolkata");
        java.time.LocalDate todayIndia = java.time.LocalDate.now(indiaZone);
        java.time.LocalTime nowIndia = java.time.LocalTime.now(indiaZone);

        if (request.getDate().isBefore(todayIndia) && !isAdmin) {
            throw new RuntimeException("Cannot edit past meal entries unless you are an Admin");
        }

        if (request.getDate().equals(todayIndia) && !isAdmin) {
            if (request.getLunch() != null) {
                java.time.LocalTime lunchCutoff = java.time.LocalTime.parse(mess.getLunchVotingDeadline());
                if (nowIndia.isAfter(lunchCutoff)) {
                    throw new RuntimeException("Lunch selection cutoff time has passed");
                }
            }
            if (request.getDinner() != null) {
                java.time.LocalTime dinnerCutoff = java.time.LocalTime.parse(mess.getDinnerVotingDeadline());
                if (nowIndia.isAfter(dinnerCutoff)) {
                    throw new RuntimeException("Dinner selection cutoff time has passed");
                }
            }
        }

        Optional<MealEntry> existingOpt = mealRepository.findByMessIdAndUserIdAndDate(messId, userId,
                request.getDate());

        Double units = 0.0;
        if (request.getLunch() != null && request.getLunch())
            units += 1.0;
        if (request.getDinner() != null && request.getDinner())
            units += 1.0;

        MealEntry entry;
        if (existingOpt.isPresent()) {
            entry = existingOpt.get();
            if (request.getLunch() != null)
                entry.setLunch(request.getLunch());
            if (request.getDinner() != null)
                entry.setDinner(request.getDinner());
            entry.setMealUnits(units);
        } else {
            entry = MealEntry.builder()
                    .messId(messId)
                    .userId(userId)
                    .date(request.getDate())
                    .lunch(request.getLunch() != null ? request.getLunch() : mess.getDefaultLunchAvailability())
                    .dinner(request.getDinner() != null ? request.getDinner() : mess.getDefaultDinnerAvailability())
                    .mealUnits(units)
                    .build();
        }

        return mealRepository.save(entry);
    }

    public MealStatusResponse getUserMealStatus(String messId, String userId, LocalDate date) {
        Mess mess = messRepository.findById(messId)
                .orElseThrow(() -> new RuntimeException("Mess not found"));

        Optional<MealEntry> existingOpt = mealRepository.findByMessIdAndUserIdAndDate(messId, userId, date);

        if (existingOpt.isPresent()) {
            return new MealStatusResponse(date, existingOpt.get().getLunch(), existingOpt.get().getDinner());
        } else {
            // Implicit YES-by-default logic if no entry exists
            return new MealStatusResponse(date, mess.getDefaultLunchAvailability(),
                    mess.getDefaultDinnerAvailability());
        }
    }

    public MealHistorySummaryResponse getMealHistory(String messId, String userId, LocalDate start, LocalDate end) {
        Mess mess = messRepository.findById(messId).orElseThrow();
        MessMember member = messMemberRepository.findByMessIdAndUserId(messId, userId).orElseThrow();
        LocalDate joinDate = member.getJoinDate() != null ? member.getJoinDate().toLocalDate() : LocalDate.MIN;

        List<MealEntry> rawEntries = mealRepository.findByMessIdAndUserIdAndDateBetween(messId, userId, start, end);
        Map<LocalDate, MealEntry> entryMap = rawEntries.stream().collect(Collectors.toMap(MealEntry::getDate, e -> e));

        List<MealStatusResponse> responses = new ArrayList<>();
        int totalLunch = 0;
        int totalDinner = 0;
        int totalMeals = 0;

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (d.isBefore(joinDate)) {
                continue;
            }
            MealEntry e = entryMap.get(d);
            boolean lunch = e != null ? e.getLunch() : mess.getDefaultLunchAvailability();
            boolean dinner = e != null ? e.getDinner() : mess.getDefaultDinnerAvailability();

            responses.add(new MealStatusResponse(d, lunch, dinner));
            if (lunch)
                totalLunch++;
            if (dinner)
                totalDinner++;
            if (lunch || dinner)
                totalMeals++;
        }

        return MealHistorySummaryResponse.builder()
                .meals(responses)
                .totalLunch(totalLunch)
                .totalDinner(totalDinner)
                .totalMeals(totalMeals)
                .build();
    }

    public AdminMealDashboardResponse getAdminMealDashboard(String messId) {
        Mess mess = messRepository.findById(messId).orElseThrow();
        List<MessMember> allMembers = messMemberRepository.findByMessId(messId);
        List<MessMember> activeMembers = allMembers.stream()
                .filter(m -> "APPROVED".equals(m.getStatus()))
                .collect(Collectors.toList());

        java.time.ZoneId indiaZone = java.time.ZoneId.of("Asia/Kolkata");
        LocalDate todayIndia = LocalDate.now(indiaZone);
        LocalTime nowIndia = LocalTime.now(indiaZone);

        LocalTime lunchCutoff = LocalTime.parse(mess.getLunchVotingDeadline());
        LocalTime dinnerCutoff = LocalTime.parse(mess.getDinnerVotingDeadline());
        String lunchStatus = nowIndia.isAfter(lunchCutoff) ? "CLOSED" : "OPEN";
        String dinnerStatus = nowIndia.isAfter(dinnerCutoff) ? "CLOSED" : "OPEN";

        List<MealEntry> todayEntries = mealRepository.findByMessIdAndDate(messId, todayIndia);
        Map<String, MealEntry> existingOptMap = todayEntries.stream()
                .collect(Collectors.toMap(MealEntry::getUserId, e -> e));

        int todayLunchYes = 0;
        int todayLunchNo = 0;
        int todayDinnerYes = 0;
        int todayDinnerNo = 0;

        for (MessMember member : activeMembers) {
            LocalDate join = member.getJoinDate() != null ? member.getJoinDate().toLocalDate() : LocalDate.MIN;
            if (todayIndia.isBefore(join))
                continue;

            MealEntry e = existingOptMap.get(member.getUserId());
            boolean lunch = e != null ? e.getLunch() : mess.getDefaultLunchAvailability();
            boolean dinner = e != null ? e.getDinner() : mess.getDefaultDinnerAvailability();

            if (lunch)
                todayLunchYes++;
            else
                todayLunchNo++;
            if (dinner)
                todayDinnerYes++;
            else
                todayDinnerNo++;
        }

        return AdminMealDashboardResponse.builder()
                .todayLunchYes(todayLunchYes)
                .todayLunchNo(todayLunchNo)
                .todayDinnerYes(todayDinnerYes)
                .todayDinnerNo(todayDinnerNo)
                .totalLunchMeals(todayLunchYes)
                .totalDinnerMeals(todayDinnerYes)
                .totalMealUnits(todayLunchYes + todayDinnerYes)
                .lunchVotingStatus(lunchStatus)
                .dinnerVotingStatus(dinnerStatus)
                .build();
    }
}
