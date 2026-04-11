package dev.nishants.appraisal.mappers;

import dev.nishants.appraisal.dtos.UserResponse;
import dev.nishants.appraisal.entity.User;

public final class UserMapper {

  private UserMapper() {
  }

  public static UserResponse toResponse(User user) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setFullName(user.getFullName());
    response.setEmail(user.getEmail());
    response.setRole(user.getRole());
    response.setJobTitle(user.getJobTitle());
    response.setActive(user.isActive());
    response.setCreatedAt(user.getCreatedAt());

    if (user.getDepartment() != null) {
      response.setDepartmentName(user.getDepartment().getName());
    }
    if (user.getManager() != null) {
      response.setManagerId(user.getManager().getId());
      response.setManagerName(user.getManager().getFullName());
    }

    return response;
  }
}
