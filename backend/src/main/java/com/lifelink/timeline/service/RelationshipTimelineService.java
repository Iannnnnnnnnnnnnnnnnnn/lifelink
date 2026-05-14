package com.lifelink.timeline.service;

import com.lifelink.timeline.dto.CreateCustomTimelineEventRequest;
import com.lifelink.timeline.dto.RelationshipTimelineEventResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RelationshipTimelineService {

    void createAutoEvent(Long relationshipId, String eventType, String title, String description, Long actorUserId,
                         String targetType, Long targetId, Long coverFileId, String coverUrl, LocalDateTime eventDate,
                         String importance, Map<String, Object> metadata);

    List<RelationshipTimelineEventResponse> listTimelineEvents(Long relationshipId, String eventType, String importance, String order, Long userId);

    RelationshipTimelineEventResponse getTimelineEventDetail(Long relationshipId, Long eventId, Long userId);

    RelationshipTimelineEventResponse createCustomEvent(Long relationshipId, CreateCustomTimelineEventRequest request, Long userId);

    void deleteTimelineEvent(Long relationshipId, Long eventId, Long userId);
}
