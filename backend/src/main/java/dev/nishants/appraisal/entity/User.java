package dev.nishants.appraisal.entity;

import java.time.LocalDateTime;

import dev.nishants.appraisal.entity.enums.Role;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "full_name", nullable = false, length = 150)
	private String fullName;
	
	@Column(name = "email", nullable = false, unique = true, length = 200)
	private String email;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length=20)
	private Role role;
	
	@Column(name = "job_title", nullable = false)
	private String jobTitle;
	
	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private boolean isActive = true;
	
	@Column(name ="created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id")
	private Department department;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id")
	private User manager;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
	
	
}
