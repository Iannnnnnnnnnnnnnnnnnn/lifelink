package com.lifelink.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceActivityResponse {

    private Long id;
    private Long relationshipId;
    private String relationshipName;
    private Long actorUserId;
    private String actorUsername;
    private String actorAvatarUrl;
    private String activityType;
    private String targetType;
    private Long targetId;
    private String title;
    private String content;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
