package dev.nishants.appraisal.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {

  public enum Status {
    NOT_STARTED,
    IN_PROGESS,
    COMPLETED,
    CANCELLED;

    @JsonCreator
    public static Status fromString(String value) {
      if (value == null)
        return null;
      if ("IN_PROGRESS".equalsIgnoreCase(value))
        return IN_PROGESS;
      return Status.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toJson() {
      return this == IN_PROGESS ? "IN_PROGRESS" : name();
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appraisal_id", nullable = false)
  private Appraisal appraisal;

  @ManyToOne(fetch = FetchType.LAZY)
  private User employee;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "progress_percent", nullable = false)
  @Builder.Default
  private Integer progressPercent = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private Status status = Status.NOT_STARTED;

  @Column(name = "due_date")
  private LocalDate dueDate;

}
