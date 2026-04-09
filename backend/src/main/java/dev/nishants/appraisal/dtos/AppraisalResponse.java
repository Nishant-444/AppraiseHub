package dev.nishants.appraisal.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.nishants.appraisal.entity.enums.AppraisalStatus;
import dev.nishants.appraisal.entity.enums.CycleStatus;

@Getter
@Setter
public class AppraisalResponse {

    private Long id;

    // Cycle info
    private String cycleName;
    private LocalDate cycleStartDate;
    private LocalDate cycleEndDate;
    private CycleStatus cycleStatus;

    // Participants
    private Long employeeId;
    private String employeeName;
    private String employeeJobTitle;
    private String employeeDepartment;
    private Long managerId;
    private String managerName;

    // Self assessment
    private String whatWentWell;
    private String whatToImprove;
    private String achievements;
    private Integer selfRating;

    // Manager review
    private String managerStrengths;
    private String managerImprovements;
    private String managerComments;
    private Integer managerRating;

    // Status & timestamps
    private AppraisalStatus appraisalStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
