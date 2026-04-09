package dev.nishants.appraisal.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.nishants.appraisal.dtos.ApiResponse;
import dev.nishants.appraisal.dtos.NotificationResponse;
import dev.nishants.appraisal.services.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Employee: get all my notifications
    // GET /api/notifications?userId=1
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(
            @RequestParam Long userId) {

        List<NotificationResponse> responses = notificationService.getMyNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Employee: mark one notification as read
    // PATCH /api/notifications/{id}/read?userId=1
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @RequestParam Long userId) {

        NotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", response));
    }

    // Employee: mark all notifications as read
    // PATCH /api/notifications/read-all?userId=1
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestParam Long userId) {

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    // Employee: get unread count (for badge counter)
    // GET /api/notifications/unread-count?userId=1
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestParam Long userId) {

        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
