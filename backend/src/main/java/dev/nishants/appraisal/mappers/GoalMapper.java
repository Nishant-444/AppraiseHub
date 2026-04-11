package dev.nishants.appraisal.mappers;

import dev.nishants.appraisal.dtos.GoalResponse;
import dev.nishants.appraisal.entity.Goal;

public final class GoalMapper {

  private GoalMapper() {
  }

  public static GoalResponse toResponse(Goal goal) {
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
