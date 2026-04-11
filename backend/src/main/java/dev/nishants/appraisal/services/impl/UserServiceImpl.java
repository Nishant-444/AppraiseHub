package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
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
import dev.nishants.appraisal.mappers.UserMapper;
import dev.nishants.appraisal.repository.DepartmentRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.AuthorizationService;
import dev.nishants.appraisal.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthorizationService authorizationService;

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
    return UserMapper.toResponse(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getMe(Long userId) {
    authorizationService.requireSelfOrHr(userId,
        "Access denied: you can only view your own profile");
    return UserMapper.toResponse(findById(userId));
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getUserById(Long userId) {
    authorizationService.requireSelfOrHr(userId,
        "Access denied: you can only view your own profile");
    return UserMapper.toResponse(findById(userId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    authorizationService.requireHr();
    return userRepository.findAllWithDetails()
        .stream()
        .map(UserMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> getTeamByManager(Long managerId) {
    authorizationService.requireSelfOrHr(
        managerId,
        "Access denied: you can only view your own team");
    return userRepository.findByManagerId(managerId)
        .stream()
        .map(UserMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public UserResponse updateUser(Long userId, UpdateUserRequest request) {
    authorizationService.requireHr();
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
    return UserMapper.toResponse(user);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    authorizationService.requireHr();
    User user = findById(userId);
    user.setActive(false);
    userRepository.save(user);
  }

  private User findById(Long id) {
    return userRepository.findByIdWithDetails(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
  }
}