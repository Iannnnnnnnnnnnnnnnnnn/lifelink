package com.lifelink.timeline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.file.entity.FileResource;
import com.lifelink.file.mapper.FileResourceMapper;
import com.lifelink.file.service.FileUrlService;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.timeline.dto.CreateCustomTimelineEventRequest;
import com.lifelink.timeline.dto.RelationshipTimelineEventResponse;
import com.lifelink.timeline.entity.RelationshipTimelineEvent;
import com.lifelink.timeline.mapper.RelationshipTimelineEventMapper;
import com.lifelink.timeline.service.RelationshipTimelineService;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipTimelineServiceImpl implements RelationshipTimelineService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String NORMAL_IMPORTANCE = "NORMAL";
    private static final String AUTO_SOURCE = "AUTO";
    private static final String MANUAL_SOURCE = "MANUAL";
    private static final String CUSTOM_EVENT = "CUSTOM";

    private final RelationshipTimelineEventMapper timelineEventMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final RelationshipMapper relationshipMapper;
    private final UserMapper userMapper;
    private final FileResourceMapper fileResourceMapper;
    private final FileUrlService fileUrlService;
    private final ObjectMapper objectMapper;

    @Override
    public void createAutoEvent(Long relationshipId, String eventType, String title, String description, Long actorUserId,
                                String targetType, Long targetId, Long coverFileId, String coverUrl, LocalDateTime eventDate,
                                String importance, Map<String, Object> metadata) {
        try {
            relationshipPermissionService.requireActiveRelationship(relationshipId);
            if (shouldSkipDuplicate(relationshipId, eventType, targetType, targetId)) {
                return;
            }
            insertEvent(relationshipId, eventType, title, description, actorUserId, targetType, targetId, coverFileId,
                    coverUrl, eventDate, importance, AUTO_SOURCE, metadata);
        } catch (Exception ex) {
            log.warn("Create relationship timeline event failed: {}", eventType, ex);
        }
    }

    @Override
    public List<RelationshipTimelineEventResponse> listTimelineEvents(Long relationshipId, String eventType, String importance, String order, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
        LambdaQueryWrapper<RelationshipTimelineEvent> wrapper = new LambdaQueryWrapper<RelationshipTimelineEvent>()
                .eq(RelationshipTimelineEvent::getRelationshipId, relationshipId)
                .eq(RelationshipTimelineEvent::getStatus, ACTIVE_STATUS);
        if (StringUtils.hasText(eventType)) {
            wrapper.eq(RelationshipTimelineEvent::getEventType, eventType);
        }
        if (StringUtils.hasText(importance)) {
            wrapper.eq(RelationshipTimelineEvent::getImportance, importance);
        }
        if ("DESC".equalsIgnoreCase(order)) {
            wrapper.orderByDesc(RelationshipTimelineEvent::getEventDate);
        } else {
            wrapper.orderByAsc(RelationshipTimelineEvent::getEventDate);
        }
        List<RelationshipTimelineEventResponse> responses = new ArrayList<RelationshipTimelineEventResponse>();
        for (RelationshipTimelineEvent event : timelineEventMapper.selectList(wrapper)) {
            responses.add(toResponse(event));
        }
        return responses;
    }

    @Override
    public RelationshipTimelineEventResponse getTimelineEventDetail(Long relationshipId, Long eventId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
        return toResponse(requireActiveEvent(relationshipId, eventId));
    }

    @Override
    @Transactional
    public RelationshipTimelineEventResponse createCustomEvent(Long relationshipId, CreateCustomTimelineEventRequest request, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
        FileResource cover = resolveCover(request.getCoverFileId(), userId);
        RelationshipTimelineEvent event = insertEvent(
                relationshipId,
                CUSTOM_EVENT,
                request.getTitle().trim(),
                trimToNull(request.getDescription()),
                userId,
                "CUSTOM",
                null,
                cover == null ? null : cover.getId(),
                cover == null ? null : fileUrlService.buildPublicUrl(cover),
                request.getEventDate(),
                request.getImportance(),
                MANUAL_SOURCE,
                Collections.emptyMap()
        );
        return toResponse(event);
    }

    @Override
    @Transactional
    public void deleteTimelineEvent(Long relationshipId, Long eventId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
        RelationshipTimelineEvent event = requireActiveEvent(relationshipId, eventId);
        event.setStatus(DELETED_STATUS);
        event.setUpdatedAt(LocalDateTime.now());
        timelineEventMapper.updateById(event);
    }

    private RelationshipTimelineEvent insertEvent(Long relationshipId, String eventType, String title, String description, Long actorUserId,
                                                  String targetType, Long targetId, Long coverFileId, String coverUrl,
                                                  LocalDateTime eventDate, String importance, String source, Map<String, Object> metadata) {
        LocalDateTime now = LocalDateTime.now();
        RelationshipTimelineEvent event = new RelationshipTimelineEvent();
        event.setRelationshipId(relationshipId);
        event.setEventType(eventType);
        event.setTitle(title);
        event.setDescription(description);
        event.setActorUserId(actorUserId);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setCoverFileId(coverFileId);
        event.setCoverUrl(coverUrl);
        event.setEventDate(eventDate == null ? now : eventDate);
        event.setImportance(StringUtils.hasText(importance) ? importance : NORMAL_IMPORTANCE);
        event.setSource(source);
        event.setMetadata(writeMetadata(metadata));
        event.setStatus(ACTIVE_STATUS);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        timelineEventMapper.insert(event);
        return event;
    }

    private boolean shouldSkipDuplicate(Long relationshipId, String eventType, String targetType, Long targetId) {
        LambdaQueryWrapper<RelationshipTimelineEvent> wrapper = new LambdaQueryWrapper<RelationshipTimelineEvent>()
                .eq(RelationshipTimelineEvent::getRelationshipId, relationshipId)
                .eq(RelationshipTimelineEvent::getEventType, eventType)
                .eq(RelationshipTimelineEvent::getStatus, ACTIVE_STATUS);
        if ("RELATIONSHIP_CREATED".equals(eventType) || "FIRST_DAILY_POST".equals(eventType)) {
            return timelineEventMapper.selectCount(wrapper) > 0;
        }
        if (StringUtils.hasText(targetType) && targetId != null) {
            return timelineEventMapper.selectCount(wrapper
                    .eq(RelationshipTimelineEvent::getTargetType, targetType)
                    .eq(RelationshipTimelineEvent::getTargetId, targetId)) > 0;
        }
        return false;
    }

    private RelationshipTimelineEvent requireActiveEvent(Long relationshipId, Long eventId) {
        RelationshipTimelineEvent event = timelineEventMapper.selectById(eventId);
        if (event == null || !relationshipId.equals(event.getRelationshipId()) || !ACTIVE_STATUS.equals(event.getStatus())) {
            throw new BusinessException(404, "Timeline event not found");
        }
        return event;
    }

    private FileResource resolveCover(Long coverFileId, Long userId) {
        if (coverFileId == null) {
            return null;
        }
        FileResource resource = fileResourceMapper.selectById(coverFileId);
        if (resource == null || !userId.equals(resource.getUserId())) {
            throw new BusinessException(400, "Cover image is invalid or not uploaded by current user");
        }
        String contentType = resource.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(400, "Cover file must be an image");
        }
        return resource;
    }

    private RelationshipTimelineEventResponse toResponse(RelationshipTimelineEvent event) {
        Relationship relationship = relationshipMapper.selectById(event.getRelationshipId());
        User actor = event.getActorUserId() == null ? null : userMapper.selectById(event.getActorUserId());
        return new RelationshipTimelineEventResponse(
                event.getId(),
                event.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                event.getEventType(),
                event.getTitle(),
                event.getDescription(),
                event.getActorUserId(),
                actor == null ? null : actor.getUsername(),
                actor == null ? null : actor.getAvatarUrl(),
                event.getTargetType(),
                event.getTargetId(),
                buildTargetUrl(event),
                event.getCoverFileId(),
                resolveCoverUrl(event),
                event.getEventDate(),
                event.getImportance(),
                event.getSource(),
                readMetadata(event.getMetadata()),
                event.getCreatedAt()
        );
    }

    private String resolveCoverUrl(RelationshipTimelineEvent event) {
        if (event.getCoverFileId() == null) {
            return event.getCoverUrl();
        }
        FileResource resource = fileResourceMapper.selectById(event.getCoverFileId());
        return resource == null ? event.getCoverUrl() : fileUrlService.buildPublicUrl(resource);
    }

    private String buildTargetUrl(RelationshipTimelineEvent event) {
        if ("RELATIONSHIP".equals(event.getTargetType())) {
            return "/relationships/" + event.getRelationshipId();
        }
        if ("USER".equals(event.getTargetType())) {
            return "/relationships/" + event.getRelationshipId();
        }
        if ("DAILY_POST".equals(event.getTargetType())) {
            return "/daily/" + event.getTargetId();
        }
        if ("SPACE_TODO".equals(event.getTargetType())) {
            return "/relationships/" + event.getRelationshipId() + "/todos";
        }
        if ("ANNIVERSARY".equals(event.getTargetType())) {
            return "/anniversaries/" + event.getTargetId();
        }
        if ("DAILY_POST_COMMENT".equals(event.getTargetType())) {
            Object postId = readMetadata(event.getMetadata()).get("postId");
            return postId == null ? null : "/daily/" + postId;
        }
        return null;
    }

    private String writeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> readMetadata(String metadata) {
        if (!StringUtils.hasText(metadata)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(metadata, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
