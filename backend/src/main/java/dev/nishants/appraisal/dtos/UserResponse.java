package dev.nishants.appraisal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.nishants.appraisal.entity.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {

  private Long id;
  private String fullName;
  private String email;
  private Role role;
  private String jobTitle;
  private String departmentName;
  private Long managerId;
  private String managerName;
  @JsonProperty("isActive")
  private boolean isActive;
  private LocalDateTime createdAt;
}
