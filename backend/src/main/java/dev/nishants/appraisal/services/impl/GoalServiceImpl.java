package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.CreateGoalRequest;
import dev.nishants.appraisal.dtos.GoalProgressRequest;
import dev.nishants.appraisal.dtos.GoalResponse;
import dev.nishants.appraisal.dtos.UpdateGoalRequest;
import dev.nishants.appraisal.entity.Appraisal;
import dev.nishants.appraisal.entity.Goal;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.enums.Role;
import dev.nishants.appraisal.exception.ResourceNotFoundException;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;
import dev.nishants.appraisal.repository.AppraisalRepository;
import dev.nishants.appraisal.repository.GoalRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.GoalService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

  private final GoalRepository goalRepository;
  private final AppraisalRepository appraisalRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public GoalResponse createGoal(CreateGoalRequest request, Long managerId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(managerId) || currentUser.getRole() != Role.MANAGER) {
      throw new UnauthorizedAccessException("Access denied: only the manager can create goals");
    }
    Appraisal appraisal = appraisalRepository.findByIdWithDetails(request.getAppraisalId())
        .orElseThrow(() -> new ResourceNotFoundException("Appraisal", request.getAppraisalId()));

    if (!appraisal.getManager().getId().equals(managerId)) {
      throw new UnauthorizedAccessException(
          "Access denied: you are not the manager for this appraisal");
    }

    Goal goal = Goal.builder()
        .appraisal(appraisal)
        .employee(appraisal.getEmployee())
        .title(request.getTitle())
        .description(request.getDescription())
        .dueDate(request.getDueDate())
        .build();

    goalRepository.save(goal);
    return mapToResponse(goal);
  }

  @Override
  @Transactional(readOnly = true)
  public GoalResponse getGoalById(Long goalId) {
    Goal goal = findById(goalId);
    User currentUser = getCurrentUser();
    if (!canViewGoal(currentUser, goal)) {
      throw new UnauthorizedAccessException("Access denied: you cannot view this goal");
    }
    return mapToResponse(goal);
  }

  @Override
  @Transactional(readOnly = true)
  public List<GoalResponse> getGoalsByAppraisal(Long appraisalId) {
    Appraisal appraisal = appraisalRepository.findByIdWithDetails(appraisalId)
        .orElseThrow(() -> new ResourceNotFoundException("Appraisal", appraisalId));
    User currentUser = getCurrentUser();
    if (!canViewAppraisalGoals(currentUser, appraisal)) {
      throw new UnauthorizedAccessException("Access denied: you cannot view these goals");
    }
    return goalRepository.findByAppraisalId(appraisalId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<GoalResponse> getGoalsByEmployee(Long employeeId) {
    User currentUser = getCurrentUser();
    if (!canViewEmployeeGoals(currentUser, employeeId)) {
      throw new UnauthorizedAccessException("Access denied: you cannot view these goals");
    }
    return goalRepository.findByEmployeeId(employeeId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public GoalResponse updateGoal(Long goalId, UpdateGoalRequest request, Long managerId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(managerId) || currentUser.getRole() != Role.MANAGER) {
      throw new UnauthorizedAccessException("Access denied: only the manager can update this goal");
    }
    Goal goal = findById(goalId);

    if (!goal.getAppraisal().getManager().getId().equals(managerId)) {
      throw new UnauthorizedAccessException(
          "Access denied: only the manager can update this goal");
    }

    if (request.getTitle() != null)
      goal.setTitle(request.getTitle());
    if (request.getDescription() != null)
      goal.setDescription(request.getDescription());
    if (request.getDueDate() != null)
      goal.setDueDate(request.getDueDate());

    goalRepository.save(goal);
    return mapToResponse(goal);
  }

  @Override
  @Transactional
  public GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(employeeId)) {
      throw new UnauthorizedAccessException("Access denied: requester mismatch");
    }
    Goal goal = findById(goalId);

    if (!goal.getEmployee().getId().equals(employeeId)) {
      throw new UnauthorizedAccessException("Access denied: this is not your goal");
    }

    goal.setStatus(request.getStatus());
    goalRepository.save(goal);
    return mapToResponse(goal);
  }

  @Override
  @Transactional
  public void deleteGoal(Long goalId, Long managerId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(managerId) || currentUser.getRole() != Role.MANAGER) {
      throw new UnauthorizedAccessException("Access denied: only the manager can delete this goal");
    }
    Goal goal = findById(goalId);

    if (!goal.getAppraisal().getManager().getId().equals(managerId)) {
      throw new UnauthorizedAccessException(
          "Access denied: only the manager can delete this goal");
    }

    goalRepository.delete(goal);
  }

  private Goal findById(Long id) {
    return goalRepository.findByIdWithDetails(id)
        .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
  }

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmailWithDetails(email)
        .orElseThrow(() -> new UnauthorizedAccessException("Access denied: user not found"));
  }

  private boolean canViewGoal(User currentUser, Goal goal) {
    if (currentUser.getRole() == Role.HR)
      return true;
    if (goal.getEmployee().getId().equals(currentUser.getId()))
      return true;
    return goal.getAppraisal().getManager().getId().equals(currentUser.getId());
  }

  private boolean canViewAppraisalGoals(User currentUser, Appraisal appraisal) {
    if (currentUser.getRole() == Role.HR)
      return true;
    if (appraisal.getEmployee().getId().equals(currentUser.getId()))
      return true;
    return appraisal.getManager().getId().equals(currentUser.getId());
  }

  private boolean canViewEmployeeGoals(User currentUser, Long employeeId) {
    if (currentUser.getRole() == Role.HR)
      return true;
    if (currentUser.getId().equals(employeeId))
      return true;
    return currentUser.getRole() == Role.MANAGER
        && userRepository.findById(employeeId)
            .map(emp -> emp.getManager() != null
                && emp.getManager().getId().equals(currentUser.getId()))
            .orElse(false);
  }

  private GoalResponse mapToResponse(Goal goal) {
    GoalResponse response = new GoalResponse();
    response.setId(goal.getId());
    response.setAppraisalId(goal.getAppraisal().getId());
    response.setEmployeeId(goal.getEmployee().getId());
    response.setEmployeeName(goal.getEmployee().getFullName());
    response.setTitle(goal.getTitle());
    response.setDescription(goal.getDescription());
    response.setStatus(goal.getStatus());
    response.setDueDate(goal.getDueDate());
    return response;
  }
}
