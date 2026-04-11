package dev.nishants.appraisal.services;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;
import dev.nishants.appraisal.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

  private final UserRepository userRepository;

  public User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmailWithDetails(email)
        .orElseThrow(() -> new UnauthorizedAccessException("Access denied: user not found"));
  }
}
