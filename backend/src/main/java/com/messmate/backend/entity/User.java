package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String googleSubjectId;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String profilePhoto;

    private String messId;

    private AccountStatus accountStatus;

    private Set<Role> roles;

    private LocalDateTime createdDate;

    private LocalDateTime lastLogin;

    // Firebase Cloud Messaging token for push notifications
    private String fcmToken;
}
