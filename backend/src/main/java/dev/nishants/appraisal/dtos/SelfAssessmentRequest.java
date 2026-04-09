package dev.nishants.appraisal.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfAssessmentRequest {

    @NotBlank(message = "Please describe what went well")
    private String whatWentWell;

    @NotBlank(message = "Please describe what you could improve")
    private String whatToImprove;

    @NotBlank(message = "Please list your key achievements")
    private String achievements;

    @NotNull(message = "Self rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer selfRating;
}
