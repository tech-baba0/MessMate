package com.messmate.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class MenuRequest {
    private Integer dayOfWeek;
    private List<String> lunchItems;
    private List<String> dinnerItems;
    private Boolean isPublished;
}
