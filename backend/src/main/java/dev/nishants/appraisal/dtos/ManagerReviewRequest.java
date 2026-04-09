package dev.nishants.appraisal.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerReviewRequest {

    @NotBlank(message = "Please describe the employee's strengths")
    private String managerStrengths;

    @NotBlank(message = "Please describe areas for improvement")
    private String managerImprovements;

    // Optional overall comments
    private String managerComments;

    @NotNull(message = "Manager rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer managerRating;
}
