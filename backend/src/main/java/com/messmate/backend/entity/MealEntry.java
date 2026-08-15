package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "meal_entries")
@CompoundIndex(def = "{'messId': 1, 'date': 1}")
@CompoundIndex(def = "{'userId': 1, 'date': 1}")
@CompoundIndex(def = "{'messId': 1, 'userId': 1, 'date': 1}", unique = true)
public class MealEntry {
    @Id
    private String id;

    private String messId;
    private String userId;

    private LocalDate date;

    private Boolean lunch;
    private Boolean dinner;

    private Double mealUnits; // e.g. Lunch=1.0, Dinner=1.0

    private Boolean isSaved;

    private java.time.LocalDateTime createdTimestamp;
    private java.time.LocalDateTime updatedTimestamp;

    private Boolean lunchOriginalStatus;
    private Boolean lunchUpdatedByAdmin;
    private java.time.LocalDateTime lunchUpdatedAt;

    private Boolean dinnerOriginalStatus;
    private Boolean dinnerUpdatedByAdmin;
    private java.time.LocalDateTime dinnerUpdatedAt;
}
