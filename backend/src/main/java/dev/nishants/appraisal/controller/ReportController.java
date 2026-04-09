package dev.nishants.appraisal.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.nishants.appraisal.dtos.*;
import dev.nishants.appraisal.services.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // HR: cycle summary — totals per status + completion % + avg rating
    // GET /api/reports/cycle/{cycleName}/summary
    @GetMapping("/cycle/{cycleName}/summary")
    public ResponseEntity<ApiResponse<CycleSummaryResponse>> getCycleSummary(
            @PathVariable String cycleName) {

        return ResponseEntity.ok(ApiResponse.success(reportService.getCycleSummary(cycleName)));
    }

    // HR: per-department breakdown for a cycle
    // GET /api/reports/cycle/{cycleName}/departments
    @GetMapping("/cycle/{cycleName}/departments")
    public ResponseEntity<ApiResponse<List<DepartmentReportResponse>>> getDepartmentReport(
            @PathVariable String cycleName) {

        return ResponseEntity.ok(ApiResponse.success(reportService.getDepartmentReport(cycleName)));
    }

    // HR: manager rating distribution 1-5 for a cycle
    // GET /api/reports/cycle/{cycleName}/ratings
    @GetMapping("/cycle/{cycleName}/ratings")
    public ResponseEntity<ApiResponse<RatingDistributionResponse>> getRatingDistribution(
            @PathVariable String cycleName) {

        return ResponseEntity.ok(ApiResponse.success(reportService.getRatingDistribution(cycleName)));
    }

    // HR: all appraisals not yet approved/acknowledged
    // GET /api/reports/cycle/{cycleName}/pending
    @GetMapping("/cycle/{cycleName}/pending")
    public ResponseEntity<ApiResponse<PendingReportResponse>> getPendingReport(
            @PathVariable String cycleName) {

        return ResponseEntity.ok(ApiResponse.success(reportService.getPendingReport(cycleName)));
    }

    // Manager: team performance for a cycle
    // GET /api/reports/manager/{managerId}/team/{cycleName}
    @GetMapping("/manager/{managerId}/team/{cycleName}")
    public ResponseEntity<ApiResponse<TeamReportResponse>> getTeamReport(
            @PathVariable Long managerId,
            @PathVariable String cycleName) {

        return ResponseEntity.ok(ApiResponse.success(reportService.getTeamReport(cycleName, managerId)));
    }

    // Employee: full appraisal history across all cycles
    // GET /api/reports/employee/{employeeId}/history
    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<ApiResponse<EmployeeHistoryResponse>> getEmployeeHistory(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(ApiResponse.success(reportService.getEmployeeHistory(employeeId)));
    }
}
