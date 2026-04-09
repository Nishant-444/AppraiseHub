package dev.nishants.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentReportResponse {
    private String departmentName;
    private long totalEmployees;
    private long completed;
    private long pending;
    private Double averageRating;
}
