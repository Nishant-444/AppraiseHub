package dev.nishants.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

import dev.nishants.appraisal.entity.enums.AppraisalStatus;

@Getter
@Builder
public class PendingReportResponse {
    private String cycleName;
    private long totalPending;
    private List<PendingEntry> entries;

    @Getter
    @Builder
    public static class PendingEntry {
        private Long employeeId;
        private String employeeName;
        private String managerName;
        private String departmentName;
        private AppraisalStatus currentStatus;
    }
}
