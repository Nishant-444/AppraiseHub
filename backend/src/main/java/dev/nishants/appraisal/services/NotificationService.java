package dev.nishants.appraisal.services;


import java.util.List;

import dev.nishants.appraisal.dtos.NotificationResponse;
import dev.nishants.appraisal.entity.Notification.Type;;

public interface NotificationService {

    void send(Long userId, String title, String message, Type type);

    List<NotificationResponse> getMyNotifications(Long userId);

    NotificationResponse markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    long countUnread(Long userId);
}
