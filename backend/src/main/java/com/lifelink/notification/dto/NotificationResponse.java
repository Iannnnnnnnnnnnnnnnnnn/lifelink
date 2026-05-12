package com.lifelink.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long receiverUserId;
    private Long actorUserId;
    private String actorUsername;
    private String actorAvatarUrl;
    private String notificationType;
    private String title;
    private String content;
    private String relatedType;
    private Long relatedId;
    private Long relationshipId;
    private String relationshipName;
    private String readStatus;
    private String status;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
