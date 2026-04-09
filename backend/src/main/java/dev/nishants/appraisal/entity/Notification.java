package dev.nishants.appraisal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  public enum Type {
    CYCLE_STARTED,
    APPRAISAL_DUE,
    SELF_ASSESSMENT_SUBMITTED,
    MANAGER_REVIEW_DONE,
    APPRAISAL_APPROVED,
    GENERAL
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 35)
  private Type type;

  @Column(name = "is_read", nullable = false)
  @Builder.Default
  private boolean isRead = false;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}
