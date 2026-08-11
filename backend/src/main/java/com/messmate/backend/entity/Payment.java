package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;

    @Indexed
    private String messId;
    private String paidById;
    private String paidToId; // ID of user or "MESS"

    private Double amount;
    private LocalDate date;

    private String method; // CASH, UPI, BANK_TRANSFER, OTHER
    private String note;

    private String status; // PENDING, COMPLETED, CANCELLED
}
