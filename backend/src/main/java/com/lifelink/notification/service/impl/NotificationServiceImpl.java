package com.lifelink.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.notification.dto.NotificationResponse;
import com.lifelink.notification.dto.NotificationUnreadCountResponse;
import com.lifelink.notification.entity.Notification;
import com.lifelink.notification.mapper.NotificationMapper;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String UNREAD_STATUS = "UNREAD";
    private static final String READ_STATUS = "READ";

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final RelationshipMapper relationshipMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void createNotification(Long receiverUserId, Long actorUserId, String notificationType, String title, String content, String relatedType, Long relatedId, Long relationshipId, Map<String, Object> metadata) {
        if (receiverUserId == null || receiverUserId.equals(actorUserId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setReceiverUserId(receiverUserId);
        notification.setActorUserId(actorUserId);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setRelationshipId(relationshipId);
        notification.setReadStatus(UNREAD_STATUS);
        notification.setStatus(ACTIVE_STATUS);
        notification.setMetadata(writeMetadata(metadata));
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    public List<NotificationResponse> listMyNotifications(String readStatus, String notificationType, Integer page, Integer size, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverUserId, userId)
                .eq(Notification::getStatus, ACTIVE_STATUS);
        if (StringUtils.hasText(readStatus)) {
            wrapper.eq(Notification::getReadStatus, readStatus);
        }
        if (StringUtils.hasText(notificationType)) {
            wrapper.eq(Notification::getNotificationType, notificationType);
        }
        wrapper.orderByDesc(Notification::getCreatedAt);
        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 20L : Math.min(size.longValue(), 100L);
        List<Notification> records = notificationMapper.selectPage(new Page<Notification>(current, pageSize), wrapper).getRecords();
        List<NotificationResponse> responses = new ArrayList<NotificationResponse>();
        for (Notification notification : records) {
            responses.add(toResponse(notification));
        }
        return responses;
    }

    @Override
    public NotificationUnreadCountResponse countUnread(Long userId) {
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverUserId, userId)
                .eq(Notification::getReadStatus, UNREAD_STATUS)
                .eq(Notification::getStatus, ACTIVE_STATUS));
        return new NotificationUnreadCountResponse(count);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = requireOwnNotification(id, userId);
        if (!READ_STATUS.equals(notification.getReadStatus())) {
            notification.setReadStatus(READ_STATUS);
            notification.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverUserId, userId)
                .eq(Notification::getReadStatus, UNREAD_STATUS)
                .eq(Notification::getStatus, ACTIVE_STATUS));
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : notifications) {
            notification.setReadStatus(READ_STATUS);
            notification.setReadAt(now);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long id, Long userId) {
        Notification notification = requireOwnNotification(id, userId);
        notification.setStatus(DELETED_STATUS);
        notificationMapper.updateById(notification);
    }

    private Notification requireOwnNotification(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !userId.equals(notification.getReceiverUserId()) || DELETED_STATUS.equals(notification.getStatus())) {
            throw new BusinessException(404, "Notification not found");
        }
        return notification;
    }

    private NotificationResponse toResponse(Notification notification) {
        User actor = notification.getActorUserId() == null ? null : userMapper.selectById(notification.getActorUserId());
        Relationship relationship = notification.getRelationshipId() == null ? null : relationshipMapper.selectById(notification.getRelationshipId());
        return new NotificationResponse(
                notification.getId(),
                notification.getReceiverUserId(),
                notification.getActorUserId(),
                actor == null ? null : actor.getUsername(),
                actor == null ? null : actor.getAvatarUrl(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedType(),
                notification.getRelatedId(),
                notification.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                notification.getReadStatus(),
                notification.getStatus(),
                readMetadata(notification.getMetadata()),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
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
}
