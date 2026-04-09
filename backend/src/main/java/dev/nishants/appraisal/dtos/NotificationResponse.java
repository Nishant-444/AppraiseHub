package dev.nishants.appraisal.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import dev.nishants.appraisal.entity.Notification.Type;

@Getter
@Setter
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private Type type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
