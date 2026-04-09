package dev.nishants.appraisal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.nishants.appraisal.dtos.ApiResponse;
import dev.nishants.appraisal.dtos.AppraisalResponse;
import dev.nishants.appraisal.dtos.BulkCycleRequest;
import dev.nishants.appraisal.dtos.BulkCycleResponse;
import dev.nishants.appraisal.dtos.CreateAppraisalRequest;
import dev.nishants.appraisal.dtos.ManagerReviewRequest;
import dev.nishants.appraisal.dtos.SelfAssessmentRequest;
import dev.nishants.appraisal.services.AppraisalService;

import java.util.List;

@RestController
@RequestMapping("/api/appraisals")
@RequiredArgsConstructor
public class AppraisalController {

    private final AppraisalService appraisalService;

    // ── HR: get all appraisals ────────────────────────────────────
    // GET /api/appraisals
    @GetMapping
    public ResponseEntity<ApiResponse<List<AppraisalResponse>>> getAllAppraisals() {
        return ResponseEntity.ok(ApiResponse.success(appraisalService.getAllAppraisals()));
    }

    // ── HR: create single appraisal ───────────────────────────────
    // POST /api/appraisals
    @PostMapping
    public ResponseEntity<ApiResponse<AppraisalResponse>> createAppraisal(
            @Valid @RequestBody CreateAppraisalRequest request) {

        AppraisalResponse response = appraisalService.createAppraisal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appraisal created successfully", response));
    }

    // ── HR: bulk create for all active employees ──────────────────
    // POST /api/appraisals/cycle/bulk-create
    @PostMapping("/cycle/bulk-create")
    public ResponseEntity<ApiResponse<BulkCycleResponse>> createBulkCycle(
            @Valid @RequestBody BulkCycleRequest request) {

        BulkCycleResponse response = appraisalService.createBulkCycle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk cycle created", response));
    }

    // ── Read ──────────────────────────────────────────────────────
    // GET /api/appraisals/my?employeeId=1
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AppraisalResponse>>> getMyAppraisals(
            @RequestParam Long employeeId) {

        return ResponseEntity.ok(ApiResponse.success(appraisalService.getMyAppraisals(employeeId)));
    }



    // GET /api/appraisals/team?managerId=1
    @GetMapping("/team")
    public ResponseEntity<ApiResponse<List<AppraisalResponse>>> getTeamAppraisals(
            @RequestParam Long managerId) {

        return ResponseEntity.ok(ApiResponse.success(appraisalService.getTeamAppraisals(managerId)));
    }

    // GET /api/appraisals/{id}?requesterId=1
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppraisalResponse>> getAppraisalById(
            @PathVariable Long id,
            @RequestParam Long requesterId) {

        return ResponseEntity.ok(ApiResponse.success(appraisalService.getAppraisalById(id, requesterId)));
    }

    // ── Employee: self-assessment draft ──────────────────────────
    // PUT /api/appraisals/{id}/self-assessment/draft?employeeId=1
    @PutMapping("/{id}/self-assessment/draft")
    public ResponseEntity<ApiResponse<AppraisalResponse>> saveSelfAssessmentDraft(
            @PathVariable Long id,
            @Valid @RequestBody SelfAssessmentRequest request,
            @RequestParam Long employeeId) {

        AppraisalResponse response = appraisalService.saveSelfAssessmentDraft(id, request, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Draft saved", response));
    }

    // ── Employee: self-assessment submit ─────────────────────────
    // PUT /api/appraisals/{id}/self-assessment/submit?employeeId=1
    @PutMapping("/{id}/self-assessment/submit")
    public ResponseEntity<ApiResponse<AppraisalResponse>> submitSelfAssessment(
            @PathVariable Long id,
            @Valid @RequestBody SelfAssessmentRequest request,
            @RequestParam Long employeeId) {

        AppraisalResponse response = appraisalService.submitSelfAssessment(id, request, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Self-assessment submitted", response));
    }

    // ── Manager: review draft ─────────────────────────────────────
    // PUT /api/appraisals/{id}/manager-review/draft?managerId=1
    @PutMapping("/{id}/manager-review/draft")
    public ResponseEntity<ApiResponse<AppraisalResponse>> saveManagerReviewDraft(
            @PathVariable Long id,
            @Valid @RequestBody ManagerReviewRequest request,
            @RequestParam Long managerId) {

        AppraisalResponse response = appraisalService.saveManagerReviewDraft(id, request, managerId);
        return ResponseEntity.ok(ApiResponse.success("Review draft saved", response));
    }

    // ── Manager: review submit ────────────────────────────────────
    // PUT /api/appraisals/{id}/manager-review/submit?managerId=1
    @PutMapping("/{id}/manager-review/submit")
    public ResponseEntity<ApiResponse<AppraisalResponse>> submitManagerReview(
            @PathVariable Long id,
            @Valid @RequestBody ManagerReviewRequest request,
            @RequestParam Long managerId) {

        AppraisalResponse response = appraisalService.submitManagerReview(id, request, managerId);
        return ResponseEntity.ok(ApiResponse.success("Manager review submitted", response));
    }

    // ── HR: approve ───────────────────────────────────────────────
    // PATCH /api/appraisals/{id}/approve
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AppraisalResponse>> approveAppraisal(
            @PathVariable Long id) {

        AppraisalResponse response = appraisalService.approveAppraisal(id);
        return ResponseEntity.ok(ApiResponse.success("Appraisal approved", response));
    }

    // ── Employee: acknowledge ─────────────────────────────────────
    // PATCH /api/appraisals/{id}/acknowledge?employeeId=1
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AppraisalResponse>> acknowledgeAppraisal(
            @PathVariable Long id,
            @RequestParam Long employeeId) {

        AppraisalResponse response = appraisalService.acknowledgeAppraisal(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Appraisal acknowledged", response));
    }
}
