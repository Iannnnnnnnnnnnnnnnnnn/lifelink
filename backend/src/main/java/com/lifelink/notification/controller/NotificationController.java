package com.lifelink.notification.controller;

import com.lifelink.common.Result;
import com.lifelink.notification.dto.NotificationResponse;
import com.lifelink.notification.dto.NotificationUnreadCountResponse;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/api/notifications")
    public Result<List<NotificationResponse>> listNotifications(
            @RequestParam(required = false) String readStatus,
            @RequestParam(required = false) String notificationType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(notificationService.listMyNotifications(readStatus, notificationType, page, size, loginUser.getId()));
    }

    @GetMapping("/api/notifications/unread-count")
    public Result<NotificationUnreadCountResponse> countUnread(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(notificationService.countUnread(loginUser.getId()));
    }

    @PatchMapping("/api/notifications/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        notificationService.markAsRead(id, loginUser.getId());
        return Result.success();
    }

    @PatchMapping("/api/notifications/read-all")
    public Result<Void> markAllAsRead(@AuthenticationPrincipal LoginUser loginUser) {
        notificationService.markAllAsRead(loginUser.getId());
        return Result.success();
    }

    @DeleteMapping("/api/notifications/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        notificationService.deleteNotification(id, loginUser.getId());
        return Result.success();
    }
}
