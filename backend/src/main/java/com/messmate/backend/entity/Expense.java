package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expenses")
public class Expense {
    @Id
    private String id;
    
    @Indexed
    private String messId;
    
    private String purchasedById;
    
    private String title;
    private LocalDate date;
    private String description;
    
    private Double totalAmount;
    
    private String splitMethod; // EQUAL, MEAL_BASED
    
    private String status; // ACTIVE, CANCELLED
    
    private List<ExpenseItem> items;
}
