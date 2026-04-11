package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.config.EmailTemplateService;
import dev.nishants.appraisal.dtos.NotificationPayload;
import dev.nishants.appraisal.dtos.NotificationResponse;
import dev.nishants.appraisal.entity.Notification;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.Notification.Type;
import dev.nishants.appraisal.exception.ResourceNotFoundException;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;
import dev.nishants.appraisal.mappers.NotificationMapper;
import dev.nishants.appraisal.repository.NotificationRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.AuthorizationService;
import dev.nishants.appraisal.services.EmailService;
import dev.nishants.appraisal.services.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;
  private final EmailTemplateService emailTemplateService;
  private final AuthorizationService authorizationService;

  @Override
  @Async
  public void send(Long userId, String title, String message, Type type,
      NotificationPayload payload) {
    sendInternal(userId, title, message, type, payload);
  }

  private void sendInternal(Long userId, String title, String message, Type type,
      NotificationPayload payload) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", userId));

    // 1. Send email before persisting the notification
    sendEmailForType(user, title, message, type, payload);

    // 2. Save in-app notification only after email succeeds
    Notification notification = Notification.builder()
        .user(user)
        .title(title)
        .message(message)
        .type(type)
        .isRead(false)
        .build();

    notificationRepository.save(notification);
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificationResponse> getMyNotifications(Long userId) {
    authorizationService.requireSelf(userId, "Access denied: you can only access your notifications");
    return notificationRepository
        .findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(NotificationMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public NotificationResponse markAsRead(Long notificationId, Long userId) {
    authorizationService.requireSelf(userId, "Access denied: you can only access your notifications");
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

    if (!notification.getUser().getId().equals(userId)) {
      throw new UnauthorizedAccessException("Access denied: this is not your notification");
    }

    notification.setRead(true);
    notificationRepository.save(notification);
    return NotificationMapper.toResponse(notification);
  }

  @Override
  @Transactional
  public void markAllAsRead(Long userId) {
    authorizationService.requireSelf(userId, "Access denied: you can only access your notifications");
    List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
    unread.forEach(n -> n.setRead(true));
    notificationRepository.saveAll(unread);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUnread(Long userId) {
    authorizationService.requireSelf(userId, "Access denied: you can only access your notifications");
    return notificationRepository.countByUserIdAndIsReadFalse(userId);
  }

  // ── Email dispatcher ──────────────────────────────────────────
  // Matches the notification type to the right email template
  private void sendEmailForType(User user, String title, String message, Type type,
      NotificationPayload payload) {
    String cycleName = resolveValue(
        payload != null ? payload.getCycleName() : null,
        extractCycleName(message));
    String employeeName = resolveValue(
        payload != null ? payload.getEmployeeName() : null,
        extractEmployeeName(message));
    String startDate = payload != null ? payload.getStartDate() : "";
    String endDate = payload != null ? payload.getEndDate() : "";

    String htmlBody = switch (type) {
      case CYCLE_STARTED ->
        emailTemplateService.cycleStarted(
            user.getFullName(),
            cycleName,
            startDate,
            endDate);
      case SELF_ASSESSMENT_SUBMITTED ->
        emailTemplateService.selfAssessmentSubmitted(
            user.getFullName(),
            employeeName,
            cycleName);
      case MANAGER_REVIEW_DONE ->
        emailTemplateService.managerReviewDone(
            user.getFullName(),
            cycleName);
      case APPRAISAL_APPROVED ->
        emailTemplateService.appraisalApproved(
            user.getFullName(),
            cycleName);
      default ->
        buildGenericEmail(user.getFullName(), title, message);
    };

    emailService.sendHtmlEmail(user.getEmail(), title, htmlBody);
  }

  // ── Simple message parsers ────────────────────────────────────
  // These extract key info from the message string we pass to send()
  // e.g. "Your appraisal for 'Q1 2025' has been created"

  private String extractCycleName(String message) {
    try {
      int start = message.indexOf("'") + 1;
      int end = message.indexOf("'", start);
      if (start > 0 && end > start)
        return message.substring(start, end);
    } catch (Exception ignored) {
    }
    return "";
  }

  private String extractEmployeeName(String message) {
    // "Alice Employee has submitted..." → "Alice Employee"
    try {
      return message.split(" has ")[0].trim();
    } catch (Exception ignored) {
    }
    return "";
  }

  private String buildGenericEmail(String name, String title, String message) {
    return """
        <html><body style="font-family:sans-serif;padding:32px;background:#f5f5f5;">
          <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;padding:32px;">
            <h2 style="color:#7c3aed;">%s</h2>
            <p>Hi <strong>%s</strong>,</p>
            <p>%s</p>
            <p style="color:#9ca3af;font-size:12px;margin-top:32px;">
              This is an automated message from AppraiseHub.
            </p>
          </div>
        </body></html>
        """.formatted(title, name, message);
  }

  private String resolveValue(String preferred, String fallback) {
    if (preferred != null && !preferred.isBlank()) {
      return preferred;
    }
    return fallback == null ? "" : fallback;
  }
}
