package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.CreateUserRequest;
import dev.nishants.appraisal.dtos.UpdateUserRequest;
import dev.nishants.appraisal.dtos.UserResponse;
import dev.nishants.appraisal.entity.Department;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.enums.Role;
import dev.nishants.appraisal.exception.DuplicateResourceException;
import dev.nishants.appraisal.exception.ResourceNotFoundException;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;
import dev.nishants.appraisal.repository.DepartmentRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException(
          "User already exists with email: " + request.getEmail());
    }

    // Role-based validation
    if (request.getRole() == Role.EMPLOYEE && request.getManagerId() == null) {
      throw new IllegalArgumentException("Employees must be assigned a manager");
    }
    if (request.getRole() == Role.HR && request.getManagerId() != null) {
      throw new IllegalArgumentException("HR users cannot have a manager assigned");
    }

    User user = User.builder()
        .fullName(request.getFullName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(request.getRole())
        .jobTitle(request.getJobTitle())
        .isActive(true)
        .build();

    if (request.getDepartmentId() != null) {
      Department dept = departmentRepository.findById(request.getDepartmentId())
          .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
      user.setDepartment(dept);
    }

    if (request.getManagerId() != null) {
      User manager = userRepository.findById(request.getManagerId())
          .orElseThrow(() -> new ResourceNotFoundException("Manager", request.getManagerId()));
      if (manager.getRole() != Role.MANAGER) {
        throw new IllegalArgumentException("Assigned manager must have the MANAGER role");
      }
      user.setManager(manager);
    }

    userRepository.save(user);
    return mapToResponse(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getMe(Long userId) {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() != Role.HR && !currentUser.getId().equals(userId)) {
      throw new UnauthorizedAccessException("Access denied: you can only view your own profile");
    }
    return mapToResponse(findById(userId));
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getUserById(Long userId) {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() != Role.HR && !currentUser.getId().equals(userId)) {
      throw new UnauthorizedAccessException("Access denied: you can only view your own profile");
    }
    return mapToResponse(findById(userId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    requireHr(getCurrentUser());
    return userRepository.findAllWithDetails()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> getTeamByManager(Long managerId) {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() != Role.HR && !currentUser.getId().equals(managerId)) {
      throw new UnauthorizedAccessException("Access denied: you can only view your own team");
    }
    return userRepository.findByManagerId(managerId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public UserResponse updateUser(Long userId, UpdateUserRequest request) {
    requireHr(getCurrentUser());
    User user = findById(userId);

    if (request.getFullName() != null)
      user.setFullName(request.getFullName());
    if (request.getJobTitle() != null)
      user.setJobTitle(request.getJobTitle());
    if (request.getIsActive() != null)
      user.setActive(request.getIsActive());

    if (request.getDepartmentId() != null) {
      Department dept = departmentRepository.findById(request.getDepartmentId())
          .orElseThrow(() -> new ResourceNotFoundException("Department", request.getDepartmentId()));
      user.setDepartment(dept);
    }

    if (request.getManagerId() != null) {
      User manager = userRepository.findById(request.getManagerId())
          .orElseThrow(() -> new ResourceNotFoundException("Manager", request.getManagerId()));
      user.setManager(manager);
    }

    userRepository.save(user);
    return mapToResponse(user);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    requireHr(getCurrentUser());
    User user = findById(userId);
    user.setActive(false);
    userRepository.save(user);
  }

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmailWithDetails(email)
        .orElseThrow(() -> new UnauthorizedAccessException("Access denied: user not found"));
  }

  private void requireHr(User user) {
    if (user.getRole() != Role.HR) {
      throw new UnauthorizedAccessException("Access denied: HR role required");
    }
  }

  private User findById(Long id) {
    return userRepository.findByIdWithDetails(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
  }

  private UserResponse mapToResponse(User user) {
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
