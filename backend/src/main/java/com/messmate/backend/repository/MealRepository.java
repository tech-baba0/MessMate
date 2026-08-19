package com.messmate.backend.repository;

import com.messmate.backend.entity.MealEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealRepository extends MongoRepository<MealEntry, String> {
    Optional<MealEntry> findByMessIdAndUserIdAndDate(String messId, String userId, LocalDate date);

    List<MealEntry> findByMessIdAndDate(String messId, LocalDate date);

    List<MealEntry> findByMessIdAndUserIdAndDateBetween(String messId, String userId, LocalDate startDate,
            LocalDate endDate);

    List<MealEntry> findByMessIdAndDateBetween(String messId, LocalDate startDate, LocalDate endDate);
}
