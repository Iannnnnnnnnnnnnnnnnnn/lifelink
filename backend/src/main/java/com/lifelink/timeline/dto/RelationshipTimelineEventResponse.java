package com.lifelink.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipTimelineEventResponse {

    private Long id;

    private Long relationshipId;

    private String relationshipName;

    private String eventType;

    private String title;

    private String description;

    private Long actorUserId;

    private String actorUsername;

    private String actorAvatarUrl;

    private String targetType;

    private Long targetId;

    private String targetUrl;

    private Long coverFileId;

    private String coverUrl;

    private LocalDateTime eventDate;

    private String importance;

    private String source;

    private Map<String, Object> metadata;

    private LocalDateTime createdAt;
}
