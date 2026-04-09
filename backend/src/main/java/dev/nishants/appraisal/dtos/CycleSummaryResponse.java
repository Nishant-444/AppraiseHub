package dev.nishants.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CycleSummaryResponse {
    private String cycleName;
    private long totalAppraisals;
    private long pending;
    private long employeeDraft;
    private long selfSubmitted;
    private long managerDraft;
    private long managerReviewed;
    private long approved;
    private long acknowledged;
    private double completionPercentage;
    private Double averageManagerRating;
}
