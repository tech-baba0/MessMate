package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messes")
public class Mess {
    @Id
    private String id;

    private String name;
    private String description;
    private String address;
    private Integer accountingStartDate; // e.g., 1st of every month

    private Boolean defaultLunchAvailability;
    private Boolean defaultDinnerAvailability;

    private String lunchVotingDeadline; // e.g., "10:00"
    private String dinnerVotingDeadline; // e.g., "17:00"

    private Integer advanceBookingDays; // default handled in logic or could be 7

    private String currency;
    private String expenseSplitMethod; // EQUAL, MEAL_BASED

    @Indexed(unique = true)
    private String inviteCode;
}
