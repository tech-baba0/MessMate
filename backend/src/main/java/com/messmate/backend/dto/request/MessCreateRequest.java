package com.messmate.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessCreateRequest {
    @NotBlank
    private String name;
    
    private String description;
    private String address;
    
    private Integer accountingStartDate = 1;
    private Boolean defaultLunchAvailability = true;
    private Boolean defaultDinnerAvailability = true;
    private String mealSelectionCutoffTime = "10:00";
    private String currency = "INR";
    private String expenseSplitMethod = "MEAL_BASED";
}
