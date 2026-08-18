package com.messmate.backend.service;

import com.messmate.backend.dto.request.MealToggleRequest;
import com.messmate.backend.dto.response.AdminMealDashboardResponse;
import com.messmate.backend.dto.response.MealHistorySummaryResponse;
import com.messmate.backend.dto.response.MealStatusResponse;
import com.messmate.backend.dto.response.MealSelectionDashboardResponse;
import com.messmate.backend.entity.MealEntry;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.entity.MessMember;
import com.messmate.backend.repository.MealRepository;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.repository.MessRepository;
import com.messmate.backend.repository.UserRepository;
import com.messmate.backend.entity.User;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FcmService fcmService;

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

        int limit = mess.getAdvanceBookingDays() != null ? mess.getAdvanceBookingDays() : 7;
        if (request.getDate().isAfter(todayIndia.plusDays(limit)) && !isAdmin) {
            throw new RuntimeException("Cannot book meals more than " + limit + " days in advance");
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
        java.time.LocalDateTime nowAction = java.time.LocalDateTime.now(indiaZone);
        if (existingOpt.isPresent()) {
            entry = existingOpt.get();
            if (request.getLunch() != null) {
                entry.setLunch(request.getLunch());
                entry.setLunchUpdatedByAdmin(isAdmin);
                entry.setLunchUpdatedAt(nowAction);
            }
            if (request.getDinner() != null) {
                entry.setDinner(request.getDinner());
                entry.setDinnerUpdatedByAdmin(isAdmin);
                entry.setDinnerUpdatedAt(nowAction);
            }
            if (request.getIsSaved() != null) {
                entry.setIsSaved(request.getIsSaved());
            } else {
                entry.setIsSaved(true);
            }
            entry.setMealUnits(units);
            entry.setUpdatedTimestamp(nowAction);
        } else {
            Boolean originalLunch = mess.getDefaultLunchAvailability();
            Boolean originalDinner = mess.getDefaultDinnerAvailability();

            entry = MealEntry.builder()
                    .messId(messId)
                    .userId(userId)
                    .date(request.getDate())
                    .lunch(request.getLunch() != null ? request.getLunch() : originalLunch)
                    .dinner(request.getDinner() != null ? request.getDinner() : originalDinner)
                    .isSaved(request.getIsSaved() != null ? request.getIsSaved() : true)
                    .mealUnits(units)
                    .createdTimestamp(nowAction)
                    .updatedTimestamp(nowAction)
                    .lunchOriginalStatus(originalLunch)
                    .dinnerOriginalStatus(originalDinner)
                    .build();

            if (request.getLunch() != null) {
                entry.setLunchUpdatedByAdmin(isAdmin);
                entry.setLunchUpdatedAt(nowAction);
            }
            if (request.getDinner() != null) {
                entry.setDinnerUpdatedByAdmin(isAdmin);
                entry.setDinnerUpdatedAt(nowAction);
            }
        }

        MealEntry savedEntry = mealRepository.save(entry);

        // Fetch user pushing update
        User actionUser = userRepository.findById(userId).orElse(null);
        String actionUserName = actionUser != null ? actionUser.getName() : "A user";

        // Build a human-readable change summary for the notification
        StringBuilder changesSb = new StringBuilder();
        if (request.getLunch() != null) {
            changesSb.append("Lunch ").append(request.getLunch() ? "\u2705 YES" : "\u274C NO");
        }
        if (request.getDinner() != null) {
            if (changesSb.length() > 0)
                changesSb.append(" | ");
            changesSb.append("Dinner ").append(request.getDinner() ? "\u2705 YES" : "\u274C NO");
        }
        String changesText = changesSb.length() > 0 ? changesSb.toString() : "No changes";

        // Notify admins
        List<MessMember> members = messMemberRepository.findByMessId(messId);
        List<MessMember> admins = members.stream()
                .filter(m -> m.getRole() == com.messmate.backend.entity.Role.ROLE_ADMIN
                        && "APPROVED".equals(m.getStatus()))
                .collect(Collectors.toList());

        for (MessMember adminMember : admins) {
            // Don't notify the admin if they are the one making the change
            if (adminMember.getUserId().equals(userId))
                continue;

            User adminUser = userRepository.findById(adminMember.getUserId()).orElse(null);
            if (adminUser != null && adminUser.getFcmToken() != null && !adminUser.getFcmToken().isEmpty()) {
                String title = "\uD83C\uDF7D Meal Update: " + actionUserName;
                String body = actionUserName + " updated meal for " + request.getDate() + ": " + changesText;

                java.util.Map<String, String> data = new java.util.HashMap<>();
                data.put("type", "MEAL_UPDATE");
                data.put("date", request.getDate().toString());

                fcmService.sendPushNotificationWithData(adminUser.getFcmToken(), title, body, data);
            }
        }

        return savedEntry;
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

    public MealSelectionDashboardResponse getMealSelectionDashboard(String messId, String userId) {
        Mess mess = messRepository.findById(messId).orElseThrow();
        int limit = mess.getAdvanceBookingDays() != null ? mess.getAdvanceBookingDays() : 7;

        java.time.ZoneId indiaZone = java.time.ZoneId.of("Asia/Kolkata");
        LocalDate todayIndia = LocalDate.now(indiaZone);
        LocalTime nowIndia = LocalTime.now(indiaZone);

        // Month Summary
        LocalDate firstDay = todayIndia.withDayOfMonth(1);
        LocalDate lastDay = todayIndia.withDayOfMonth(todayIndia.lengthOfMonth());
        MealHistorySummaryResponse monthSummary = getMealHistory(messId, userId, firstDay, lastDay);

        // Recent history: last 5 days
        LocalDate fiveDaysAgo = todayIndia.minusDays(5);
        LocalDate yesterday = todayIndia.minusDays(1);
        List<MealStatusResponse> recentHistoryList = new ArrayList<>();
        if (!fiveDaysAgo.isAfter(yesterday)) {
            recentHistoryList = getMealHistory(messId, userId, fiveDaysAgo, yesterday).getMeals();
        }

        // Future Selections: today to today + limit
        LocalDate futureEnd = todayIndia.plusDays(limit);
        List<MealStatusResponse> futureList = getMealHistory(messId, userId, todayIndia, futureEnd).getMeals();

        return MealSelectionDashboardResponse.builder()
                .advanceBookingDays(limit)
                .lunchVotingDeadline(mess.getLunchVotingDeadline())
                .dinnerVotingDeadline(mess.getDinnerVotingDeadline())
                .currentServerTime(nowIndia.toString())
                .currentMonthTotalMeals(monthSummary.getTotalMeals())
                .currentMonthLunchCount(monthSummary.getTotalLunch())
                .currentMonthDinnerCount(monthSummary.getTotalDinner())
                .recentHistory(recentHistoryList)
                .futureSelections(futureList)
                .build();
    }

    public AdminMealDashboardResponse getAdminMealDashboard(String messId, LocalDate targetDate) {
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

        String lunchStatus = "OPEN";
        String dinnerStatus = "OPEN";
        if (targetDate.isBefore(todayIndia)) {
            lunchStatus = "CLOSED";
            dinnerStatus = "CLOSED";
        } else if (targetDate.equals(todayIndia)) {
            lunchStatus = nowIndia.isAfter(lunchCutoff) ? "CLOSED" : "OPEN";
            dinnerStatus = nowIndia.isAfter(dinnerCutoff) ? "CLOSED" : "OPEN";
        }

        List<MealEntry> targetEntries = mealRepository.findByMessIdAndDate(messId, targetDate);
        Map<String, MealEntry> existingOptMap = targetEntries.stream()
                .collect(Collectors.toMap(MealEntry::getUserId, e -> e));

        int todayLunchYes = 0;
        int todayLunchNo = 0;
        int todayDinnerYes = 0;
        int todayDinnerNo = 0;

        List<com.messmate.backend.dto.response.MemberMealDetailResponse> detailedRecords = new ArrayList<>();
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");

        for (MessMember member : activeMembers) {
            LocalDate join = member.getJoinDate() != null ? member.getJoinDate().toLocalDate() : LocalDate.MIN;
            if (targetDate.isBefore(join))
                continue;

            MealEntry e = existingOptMap.get(member.getUserId());

            boolean lunch;
            boolean dinner;
            // lunchIsDefault = true means user never explicitly voted (counted as default
            // YES)
            boolean lunchIsDefault;
            boolean dinnerIsDefault;

            if (e == null) {
                // No DB record — using mess-wide default
                lunch = mess.getDefaultLunchAvailability() != null ? mess.getDefaultLunchAvailability() : true;
                dinner = mess.getDefaultDinnerAvailability() != null ? mess.getDefaultDinnerAvailability() : true;
                lunchIsDefault = true;
                dinnerIsDefault = true;
            } else {
                lunch = e.getLunch();
                dinner = e.getDinner();
                // If updatedAt is null for a meal type, it was never explicitly touched
                lunchIsDefault = (e.getLunchUpdatedAt() == null);
                dinnerIsDefault = (e.getDinnerUpdatedAt() == null);
            }

            if (lunch)
                todayLunchYes++;
            else
                todayLunchNo++;

            if (dinner)
                todayDinnerYes++;
            else
                todayDinnerNo++;

            String lunchTime = lunchIsDefault ? "Default" : "Unknown";
            String dinnerTime = dinnerIsDefault ? "Default" : "Unknown";

            if (e != null) {
                if (e.getLunchUpdatedAt() != null) {
                    lunchTime = e.getLunchUpdatedAt().format(timeFormatter);
                } else if (!lunchIsDefault && e.getUpdatedTimestamp() != null) {
                    lunchTime = e.getUpdatedTimestamp().format(timeFormatter);
                }

                if (e.getDinnerUpdatedAt() != null) {
                    dinnerTime = e.getDinnerUpdatedAt().format(timeFormatter);
                } else if (!dinnerIsDefault && e.getUpdatedTimestamp() != null) {
                    dinnerTime = e.getUpdatedTimestamp().format(timeFormatter);
                }
            }

            User user = userRepository.findById(member.getUserId()).orElse(null);
            String userName = user != null ? user.getName() : "Unknown";

            detailedRecords.add(com.messmate.backend.dto.response.MemberMealDetailResponse.builder()
                    .userName(userName)
                    .lunch(lunch)
                    .dinner(dinner)
                    .lunchUpdatedAt(lunchTime)
                    .dinnerUpdatedAt(dinnerTime)
                    .lunchIsDefault(lunchIsDefault)
                    .dinnerIsDefault(dinnerIsDefault)
                    .build());
        }

        return AdminMealDashboardResponse.builder()
                .targetDate(targetDate)
                .totalActiveMembers(activeMembers.size())
                .todayLunchYes(todayLunchYes)
                .todayLunchNo(todayLunchNo)
                .todayDinnerYes(todayDinnerYes)
                .todayDinnerNo(todayDinnerNo)
                .totalLunchMeals(todayLunchYes)
                .totalDinnerMeals(todayDinnerYes)
                .totalMealUnits(todayLunchYes + todayDinnerYes)
                .lunchVotingStatus(lunchStatus)
                .dinnerVotingStatus(dinnerStatus)
                .memberDetails(detailedRecords)
                .build();
    }
}
