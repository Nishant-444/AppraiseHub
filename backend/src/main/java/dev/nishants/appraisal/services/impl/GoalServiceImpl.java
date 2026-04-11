package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.GoalProgressRequest;
import dev.nishants.appraisal.dtos.GoalRequest;
import dev.nishants.appraisal.dtos.GoalResponse;
import dev.nishants.appraisal.entity.Appraisal;
import dev.nishants.appraisal.entity.Goal;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.enums.Role;
import dev.nishants.appraisal.exception.ResourceNotFoundException;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;
import dev.nishants.appraisal.mappers.GoalMapper;
import dev.nishants.appraisal.repository.AppraisalRepository;
import dev.nishants.appraisal.repository.GoalRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.AuthorizationService;
import dev.nishants.appraisal.services.GoalService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

  private final GoalRepository goalRepository;
  private final AppraisalRepository appraisalRepository;
  private final UserRepository userRepository;
  private final AuthorizationService authorizationService;

  @Override
  @Transactional
  public GoalResponse createGoal(GoalRequest request, Long managerId) {
    authorizationService.requireManagerSelf(
        managerId,
        "Access denied: only the manager can create goals");
    validateCreateGoalRequest(request);
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
    return GoalMapper.toResponse(goal);
  }

  @Override
  @Transactional(readOnly = true)
  public GoalResponse getGoalById(Long goalId) {
    Goal goal = findById(goalId);
    User currentUser = authorizationService.currentUser();
    if (!canViewGoal(currentUser, goal)) {
      throw new UnauthorizedAccessException("Access denied: you cannot view this goal");
    }
    return GoalMapper.toResponse(goal);
  }

  @Override
  @Transactional(readOnly = true)
  public List<GoalResponse> getGoalsByAppraisal(Long appraisalId) {
    Appraisal appraisal = appraisalRepository.findByIdWithDetails(appraisalId)
        .orElseThrow(() -> new ResourceNotFoundException("Appraisal", appraisalId));
    User currentUser = authorizationService.currentUser();
    if (!canViewAppraisalGoals(currentUser, appraisal)) {
      throw new UnauthorizedAccessException("Access denied: you cannot view these goals");
    }
    return goalRepository.findByAppraisalId(appraisalId)
        .stream()
        .map(GoalMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<GoalResponse> getGoalsByEmployee(Long employeeId) {
    User currentUser = authorizationService.currentUser();
    if (!canViewEmployeeGoals(currentUser, employeeId)) {
      throw new UnauthorizedAccessException("Access denied: you cannot view these goals");
    }
    return goalRepository.findByEmployeeId(employeeId)
        .stream()
        .map(GoalMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public GoalResponse updateGoal(Long goalId, GoalRequest request, Long managerId) {
    authorizationService.requireManagerSelf(
        managerId,
        "Access denied: only the manager can update this goal");
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
    return GoalMapper.toResponse(goal);
  }

  @Override
  @Transactional
  public GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId) {
    authorizationService.requireSelf(employeeId, "Access denied: requester mismatch");
    Goal goal = findById(goalId);

    if (!goal.getEmployee().getId().equals(employeeId)) {
      throw new UnauthorizedAccessException("Access denied: this is not your goal");
    }

    goal.setStatus(request.getStatus());
    goalRepository.save(goal);
    return GoalMapper.toResponse(goal);
  }

  @Override
  @Transactional
  public void deleteGoal(Long goalId, Long managerId) {
    authorizationService.requireManagerSelf(
        managerId,
        "Access denied: only the manager can delete this goal");
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

  private boolean canViewGoal(User currentUser, Goal goal) {
    if (authorizationService.isHr(currentUser))
      return true;
    if (goal.getEmployee().getId().equals(currentUser.getId()))
      return true;
    return goal.getAppraisal().getManager().getId().equals(currentUser.getId());
  }

  private boolean canViewAppraisalGoals(User currentUser, Appraisal appraisal) {
    if (authorizationService.isHr(currentUser))
      return true;
    if (appraisal.getEmployee().getId().equals(currentUser.getId()))
      return true;
    return appraisal.getManager().getId().equals(currentUser.getId());
  }

  private boolean canViewEmployeeGoals(User currentUser, Long employeeId) {
    if (authorizationService.isHr(currentUser))
      return true;
    if (currentUser.getId().equals(employeeId))
      return true;
    return currentUser.getRole() == Role.MANAGER
        && userRepository.findById(employeeId)
            .map(emp -> emp.getManager() != null
                && emp.getManager().getId().equals(currentUser.getId()))
            .orElse(false);
  }

  private void validateCreateGoalRequest(GoalRequest request) {
    if (request.getAppraisalId() == null) {
      throw new IllegalArgumentException("Appraisal ID is required");
    }
    if (request.getTitle() == null || request.getTitle().isBlank()) {
      throw new IllegalArgumentException("Goal title is required");
    }
  }

}
