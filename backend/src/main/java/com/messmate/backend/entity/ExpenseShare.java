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
@Document(collection = "expense_shares")
public class ExpenseShare {
    @Id
    private String id;
    
    @Indexed
    private String expenseId;
    
    @Indexed
    private String messId;
    
    @Indexed
    private String userId;
    
    private Double shareAmount;
}
