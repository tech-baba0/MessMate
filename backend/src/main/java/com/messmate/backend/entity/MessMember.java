package com.messmate.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mess_members")
@CompoundIndex(def = "{'messId': 1, 'userId': 1}", unique = true)
public class MessMember {
    @Id
    private String id;
    
    private String messId;
    private String userId;
    
    private Role role; // ROLE_ADMIN, ROLE_USER
    
    private String status; // PENDING, APPROVED, REJECTED
    
    private LocalDateTime joinDate;
}
