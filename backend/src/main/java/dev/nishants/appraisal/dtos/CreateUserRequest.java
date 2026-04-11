package dev.nishants.appraisal.dtos;

import dev.nishants.appraisal.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Must be a valid email")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotNull(message = "Role is required")
  private Role role;

  private String jobTitle;

  private Long departmentId;

  private Long managerId;
}
