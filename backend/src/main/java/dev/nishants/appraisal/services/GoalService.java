package dev.nishants.appraisal.services;



import java.util.List;

import dev.nishants.appraisal.dtos.CreateGoalRequest;
import dev.nishants.appraisal.dtos.GoalProgressRequest;
import dev.nishants.appraisal.dtos.GoalResponse;
import dev.nishants.appraisal.dtos.UpdateGoalRequest;

public interface GoalService {

    GoalResponse createGoal(CreateGoalRequest request, Long managerId);

    GoalResponse getGoalById(Long goalId);

    List<GoalResponse> getGoalsByAppraisal(Long appraisalId);

    List<GoalResponse> getGoalsByEmployee(Long employeeId);

    GoalResponse updateGoal(Long goalId, UpdateGoalRequest request, Long managerId);

    GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId);

    void deleteGoal(Long goalId, Long managerId);
}

