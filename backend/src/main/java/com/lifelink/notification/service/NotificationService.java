package com.lifelink.notification.service;

import com.lifelink.notification.dto.NotificationResponse;
import com.lifelink.notification.dto.NotificationUnreadCountResponse;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    void createNotification(Long receiverUserId, Long actorUserId, String notificationType, String title, String content, String relatedType, Long relatedId, Long relationshipId, Map<String, Object> metadata);

    List<NotificationResponse> listMyNotifications(String readStatus, String notificationType, Integer page, Integer size, Long userId);

    NotificationUnreadCountResponse countUnread(Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long id, Long userId);
}
