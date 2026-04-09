package dev.nishants.appraisal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.nishants.appraisal.dtos.*;
import dev.nishants.appraisal.services.GoalService;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    // POST /api/goals?managerId=1
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @Valid @RequestBody CreateGoalRequest request,
            @RequestParam Long managerId) {

        GoalResponse response = goalService.createGoal(request, managerId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Goal created successfully", response));
    }

    // GET /api/goals/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalById(id)));
    }

    // GET /api/goals/appraisal/{appraisalId}
    @GetMapping("/appraisal/{appraisalId}")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByAppraisal(
            @PathVariable Long appraisalId) {
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalsByAppraisal(appraisalId)));
    }

    // GET /api/goals/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoalsByEmployee(employeeId)));
    }

    // PUT /api/goals/{id}?managerId=1
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long id,
            @RequestBody UpdateGoalRequest request,
            @RequestParam Long managerId) {

        GoalResponse response = goalService.updateGoal(id, request, managerId);
        return ResponseEntity.ok(ApiResponse.success("Goal updated successfully", response));
    }

    // PATCH /api/goals/{id}/progress?employeeId=1
    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalResponse>> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody GoalProgressRequest request,
            @RequestParam Long employeeId) {

        GoalResponse response = goalService.updateProgress(id, request, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Goal progress updated", response));
    }

    // DELETE /api/goals/{id}?managerId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable Long id,
            @RequestParam Long managerId) {

        goalService.deleteGoal(id, managerId);
        return ResponseEntity.ok(ApiResponse.success("Goal deleted successfully", null));
    }
}
