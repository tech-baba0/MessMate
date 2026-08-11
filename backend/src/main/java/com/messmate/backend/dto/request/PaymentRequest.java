package com.messmate.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PaymentRequest {
    private String paidToId;

    @NotNull
    private Double amount;

    @NotNull
    private LocalDate date;

    private String method;
    private String note;
}
