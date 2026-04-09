package dev.nishants.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

import dev.nishants.appraisal.entity.enums.AppraisalStatus;

@Getter
@Builder
public class EmployeeHistoryResponse {
    private Long employeeId;
    private String employeeName;
    private List<CycleRecord> cycles;

    @Getter
    @Builder
    public static class CycleRecord {
        private String cycleName;
        private LocalDate cycleStartDate;
        private LocalDate cycleEndDate;
        private Integer selfRating;
        private Integer managerRating;
        private AppraisalStatus status;
        private String managerName;
    }
}
