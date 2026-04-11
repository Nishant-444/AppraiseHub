package dev.nishants.appraisal.mappers;

import dev.nishants.appraisal.dtos.NotificationResponse;
import dev.nishants.appraisal.entity.Notification;

public final class NotificationMapper {

  private NotificationMapper() {
  }

  public static NotificationResponse toResponse(Notification notification) {
    NotificationResponse response = new NotificationResponse();
    response.setId(notification.getId());
    response.setTitle(notification.getTitle());
    response.setMessage(notification.getMessage());
    response.setType(notification.getType());
    response.setRead(notification.isRead());
    response.setCreatedAt(notification.getCreatedAt());
    return response;
  }
}
