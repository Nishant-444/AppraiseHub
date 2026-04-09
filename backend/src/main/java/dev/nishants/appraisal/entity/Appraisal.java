package dev.nishants.appraisal.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.nishants.appraisal.entity.enums.AppraisalStatus;
import dev.nishants.appraisal.entity.enums.CycleStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appraisals", uniqueConstraints = @UniqueConstraint(name = "uq_cycle_employee", columnNames = {
		"cycle_name", "employee_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appraisal {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "cycle_name", nullable = false, length = 150)
	private String cycleName;
	
	@Column(name = "cycle_start_date", nullable = false)
	private LocalDate cycleStartDate;
	
	@Column(name = "cycle_end_date", nullable = false)
	private LocalDate cycleEndDate;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "cycle_status", nullable = false, length = 20)
	@Builder.Default
	private CycleStatus cycleStatus = CycleStatus.DRAFT;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="employee_id", nullable = false)
	private User employee;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id", nullable = false)
	private User manager;
	
	
	// Self assessment fields
	@Column(name = "what_went_well", columnDefinition = "TEXT")
	private String whatWentWell;

	@Column(name = "what_to_improve", columnDefinition = "TEXT")
	private String whatToImprove;

	@Column(name = "achievements", columnDefinition = "TEXT")
	private String achievements;

	@Column(name = "self_rating")
	private Integer selfRating;

	// Manager review fields
	@Column(name = "manager_strengths", columnDefinition = "TEXT")
	private String managerStrengths;

	@Column(name = "manager_improvements", columnDefinition = "TEXT")
	private String managerImprovements;

	@Column(name = "manager_comments", columnDefinition = "TEXT")
	private String managerComments;

	@Column(name = "manager_rating")
	private Integer managerRating;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "appraisal_status", nullable = false, length = 25)
	@Builder.Default
	private AppraisalStatus appraisalStatus = AppraisalStatus.PENDING;
	
	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;
	
	@Column(name = "approved_at")
	private LocalDateTime approvedAt;
	
	@Column(name ="created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
	
}
