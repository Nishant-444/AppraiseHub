package dev.nishants.appraisal.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateGoalRequest {

    @NotNull(message = "Appraisal ID is required")
    private Long appraisalId;

    @NotBlank(message = "Goal title is required")
    private String title;

    private String description;

    private LocalDate dueDate;
}
