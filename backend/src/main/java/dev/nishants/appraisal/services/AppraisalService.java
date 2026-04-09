package dev.nishants.appraisal.services;

import java.util.List;

import dev.nishants.appraisal.dtos.AppraisalResponse;
import dev.nishants.appraisal.dtos.BulkCycleRequest;
import dev.nishants.appraisal.dtos.BulkCycleResponse;
import dev.nishants.appraisal.dtos.CreateAppraisalRequest;
import dev.nishants.appraisal.dtos.ManagerReviewRequest;
import dev.nishants.appraisal.dtos.SelfAssessmentRequest;

public interface AppraisalService {

    // HR: view all appraisals across all cycles
    List<AppraisalResponse> getAllAppraisals();

    // HR: create a new appraisal for an employee in a cycle
    AppraisalResponse createAppraisal(CreateAppraisalRequest request);

    // HR: bulk create one appraisal per active employee for a cycle
    BulkCycleResponse createBulkCycle(BulkCycleRequest request);

    // Employee: view all their own appraisals
    List<AppraisalResponse> getMyAppraisals(Long employeeId);

    // Manager: view all appraisals for their team
    List<AppraisalResponse> getTeamAppraisals(Long managerId);

    // Any role: view one appraisal by ID (with ownership check)
    AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId);

    // Employee: save draft — status stays EMPLOYEE_DRAFT, no notification
    AppraisalResponse saveSelfAssessmentDraft(Long appraisalId, SelfAssessmentRequest request, Long employeeId);

    // Employee: final submit — status moves to SELF_SUBMITTED, notifies manager
    AppraisalResponse submitSelfAssessment(Long appraisalId, SelfAssessmentRequest request, Long employeeId);

    // Manager: save draft — status stays MANAGER_DRAFT, no notification
    AppraisalResponse saveManagerReviewDraft(Long appraisalId, ManagerReviewRequest request, Long managerId);

    // Manager: final submit — status moves to MANAGER_REVIEWED, notifies HR + employee
    AppraisalResponse submitManagerReview(Long appraisalId, ManagerReviewRequest request, Long managerId);

    // HR: approve final appraisal — moves status to APPROVED
    AppraisalResponse approveAppraisal(Long appraisalId);

    // Employee: acknowledge result — moves status to ACKNOWLEDGED
    AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId);
}
