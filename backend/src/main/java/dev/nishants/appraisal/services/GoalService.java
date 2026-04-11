package dev.nishants.appraisal.services;

import java.util.List;

import dev.nishants.appraisal.dtos.GoalRequest;
import dev.nishants.appraisal.dtos.GoalProgressRequest;
import dev.nishants.appraisal.dtos.GoalResponse;

public interface GoalService {

  GoalResponse createGoal(GoalRequest request, Long managerId);

  GoalResponse getGoalById(Long goalId);

  List<GoalResponse> getGoalsByAppraisal(Long appraisalId);

  List<GoalResponse> getGoalsByEmployee(Long employeeId);

  GoalResponse updateGoal(Long goalId, GoalRequest request, Long managerId);

  GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId);

  void deleteGoal(Long goalId, Long managerId);
}
