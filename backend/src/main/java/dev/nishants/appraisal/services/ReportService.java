package dev.nishants.appraisal.services;

import java.util.List;

import dev.nishants.appraisal.dtos.*;

public interface ReportService {

    CycleSummaryResponse getCycleSummary(String cycleName);

    List<DepartmentReportResponse> getDepartmentReport(String cycleName);

    RatingDistributionResponse getRatingDistribution(String cycleName);

    PendingReportResponse getPendingReport(String cycleName);

    TeamReportResponse getTeamReport(String cycleName, Long managerId);

    EmployeeHistoryResponse getEmployeeHistory(Long employeeId);
}
