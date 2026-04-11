package dev.nishants.appraisal.mappers;

import dev.nishants.appraisal.dtos.AppraisalResponse;
import dev.nishants.appraisal.entity.Appraisal;

public final class AppraisalMapper {

  private AppraisalMapper() {
  }

  public static AppraisalResponse toResponse(Appraisal appraisal) {
    AppraisalResponse response = new AppraisalResponse();
    response.setId(appraisal.getId());
    response.setCycleName(appraisal.getCycleName());
    response.setCycleStartDate(appraisal.getCycleStartDate());
    response.setCycleEndDate(appraisal.getCycleEndDate());
    response.setCycleStatus(appraisal.getCycleStatus());
    response.setEmployeeId(appraisal.getEmployee().getId());
    response.setEmployeeName(appraisal.getEmployee().getFullName());
    response.setEmployeeJobTitle(appraisal.getEmployee().getJobTitle());
    if (appraisal.getEmployee().getDepartment() != null) {
      response.setEmployeeDepartment(appraisal.getEmployee().getDepartment().getName());
    }
    response.setManagerId(appraisal.getManager().getId());
    response.setManagerName(appraisal.getManager().getFullName());
    response.setWhatWentWell(appraisal.getWhatWentWell());
    response.setWhatToImprove(appraisal.getWhatToImprove());
    response.setAchievements(appraisal.getAchievements());
    response.setSelfRating(appraisal.getSelfRating());
    response.setManagerStrengths(appraisal.getManagerStrengths());
    response.setManagerImprovements(appraisal.getManagerImprovements());
    response.setManagerComments(appraisal.getManagerComments());
    response.setManagerRating(appraisal.getManagerRating());
    response.setAppraisalStatus(appraisal.getAppraisalStatus());
    response.setSubmittedAt(appraisal.getSubmittedAt());
    response.setApprovedAt(appraisal.getApprovedAt());
    response.setCreatedAt(appraisal.getCreatedAt());
    return response;
  }
}
