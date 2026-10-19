package com.Prakhar.Notification_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private String username;
    private String email;
    private String userId;
    private String resetLink;
    private String eventType; // "USER_CREATED"
}