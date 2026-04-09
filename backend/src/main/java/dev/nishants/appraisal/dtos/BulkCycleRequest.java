package dev.nishants.appraisal.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BulkCycleRequest {

    @NotBlank(message = "Cycle name is required")
    private String cycleName;

    @NotNull(message = "Cycle start date is required")
    private LocalDate cycleStartDate;

    @NotNull(message = "Cycle end date is required")
    private LocalDate cycleEndDate;

    // Optional — if set, only create for employees in this department
    private Long departmentId;
}
