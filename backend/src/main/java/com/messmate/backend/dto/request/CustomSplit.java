package com.messmate.backend.dto.request;

import lombok.Data;

@Data
public class CustomSplit {
    private String memberId;
    private Double amount;
    private Double percentage;
}
