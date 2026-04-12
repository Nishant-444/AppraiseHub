package dev.nishants.appraisal.services;

import java.util.List;

import dev.nishants.appraisal.dtos.AppraisalResponse;
import dev.nishants.appraisal.dtos.BulkCycleRequest;
import dev.nishants.appraisal.dtos.BulkCycleResponse;
import dev.nishants.appraisal.dtos.CreateAppraisalRequest;
import dev.nishants.appraisal.dtos.ManagerReviewRequest;
import dev.nishants.appraisal.dtos.SelfAssessmentRequest;

public interface AppraisalService {

  // flow of an appraisal->
  // HR -> create appraisal
  AppraisalResponse createAppraisal(CreateAppraisalRequest request);

  // HR -> view all appraisals
  List<AppraisalResponse> getAllAppraisals();

  // HR -> create bulk cycle
  BulkCycleResponse createBulkCycle(BulkCycleRequest request);

  // Employee -> view their own appraisals
  List<AppraisalResponse> getMyAppraisals(Long employeeId);

  // Manager -> view appraisals of their team
  List<AppraisalResponse> getTeamAppraisals(Long managerId);

  // View appraisal details (for employee, manager, or HR based on requester role)
  AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId);

  // Employee -> save self-assessment draft, no notification
  AppraisalResponse saveSelfAssessmentDraft(Long appraisalId, SelfAssessmentRequest request, Long employeeId);

  // Employee -> final submit self-assessment,notification to manager
  AppraisalResponse submitSelfAssessment(Long appraisalId, SelfAssessmentRequest request, Long employeeId);

  // Manager -> save manager review draft, no notification
  AppraisalResponse saveManagerReviewDraft(Long appraisalId, ManagerReviewRequest request, Long managerId);

  // Manager -> final submit manager review, notification to employee and HR
  AppraisalResponse submitManagerReview(Long appraisalId, ManagerReviewRequest request, Long managerId);

  // HR -> approve appraisal, notification to employee and manager
  AppraisalResponse approveAppraisal(Long appraisalId);

  // Employee -> acknowledge appraisal, notification to HR and manager
  AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId);
}
