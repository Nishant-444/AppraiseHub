package dev.nishants.appraisal.services;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.enums.Role;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

  private final CurrentUserService currentUserService;

  public User currentUser() {
    return currentUserService.getCurrentUser();
  }

  public User requireHr() {
    User user = currentUserService.getCurrentUser();
    if (user.getRole() != Role.HR) {
      throw new UnauthorizedAccessException("Access denied: HR role required");
    }
    return user;
  }

  public User requireSelf(Long userId, String message) {
    User user = currentUserService.getCurrentUser();
    if (!user.getId().equals(userId)) {
      throw new UnauthorizedAccessException(message);
    }
    return user;
  }

  public User requireSelfOrHr(Long userId, String message) {
    User user = currentUserService.getCurrentUser();
    if (user.getRole() != Role.HR && !user.getId().equals(userId)) {
      throw new UnauthorizedAccessException(message);
    }
    return user;
  }

  public User requireManagerSelf(Long managerId, String message) {
    User user = currentUserService.getCurrentUser();
    if (user.getRole() != Role.MANAGER || !user.getId().equals(managerId)) {
      throw new UnauthorizedAccessException(message);
    }
    return user;
  }

  public boolean isHr(User user) {
    return user.getRole() == Role.HR;
  }
}
