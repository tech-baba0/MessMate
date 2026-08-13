package com.messmate.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberMealDetailResponse {
    private String userName;
    private boolean lunch;
    private boolean dinner;
    private String lunchUpdatedAt;
    private String dinnerUpdatedAt;
}
