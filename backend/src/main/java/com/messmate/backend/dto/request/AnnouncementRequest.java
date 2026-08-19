package com.messmate.backend.dto.request;

import lombok.Data;

@Data
public class AnnouncementRequest {
    private String title;
    private String message;
    private String targetUserId; // null or empty means send to ALL active members
}
