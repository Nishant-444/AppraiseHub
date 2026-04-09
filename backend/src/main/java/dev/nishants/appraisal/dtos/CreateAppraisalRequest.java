package dev.nishants.appraisal.dtos;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class CreateAppraisalRequest {

    @NotBlank(message = "Cycle name is required")
    private String cycleName;

    @NotNull(message = "Cycle start date is required")
    private LocalDate cycleStartDate;

    @NotNull(message = "Cycle end date is required")
    private LocalDate cycleEndDate;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Manager ID is required")
    private Long managerId;
}
