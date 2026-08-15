package com.messmate.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MealToggleRequest {
    @NotNull
    private LocalDate date;

    private Boolean lunch;
    private Boolean dinner;
    private Boolean isSaved;
}
