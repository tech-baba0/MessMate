package com.messmate.backend.service;

import com.messmate.backend.dto.request.MealToggleRequest;
import com.messmate.backend.dto.response.MealStatusResponse;
import com.messmate.backend.entity.MealEntry;
import com.messmate.backend.entity.Mess;
import com.messmate.backend.repository.MealRepository;
import com.messmate.backend.repository.MessMemberRepository;
import com.messmate.backend.repository.MessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

        if (LocalDate.now().equals(request.getDate())) {
            LocalTime cutoff = LocalTime.parse(mess.getMealSelectionCutoffTime());
            if (LocalTime.now().isAfter(cutoff)) {
                throw new RuntimeException("Meal selection cutoff time has passed");
            }
        }

        if (request.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot edit past meal entries");
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

    public List<MealEntry> getMealHistory(String messId, String userId, LocalDate start, LocalDate end) {
        return mealRepository.findByMessIdAndUserIdAndDateBetween(messId, userId, start, end);
    }
}
