package dev.nishants.appraisal.dtos;

import dev.nishants.appraisal.entity.Goal.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalProgressRequest {

    @NotNull(message = "Status is required")
    private Status status;
}
