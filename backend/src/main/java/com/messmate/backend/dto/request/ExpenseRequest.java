package com.messmate.backend.dto.request;

import com.messmate.backend.entity.ExpenseItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    @NotNull
    private LocalDate date;
    
    @NotNull
    private Double totalAmount;
    
    private String splitMethod = "MEAL_BASED"; // EQUAL, MEAL_BASED
    
    private List<ExpenseItem> items;
}
