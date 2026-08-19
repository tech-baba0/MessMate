package com.messmate.backend.dto.request;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class MenuRequest {
    private Integer dayOfWeek;
    private List<String> lunchItems;
    private List<String> dinnerItems;
    @JsonProperty("isPublished")
    private Boolean isPublished;
}
