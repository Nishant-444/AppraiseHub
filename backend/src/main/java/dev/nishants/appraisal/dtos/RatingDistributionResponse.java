package dev.nishants.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class RatingDistributionResponse {
    private String cycleName;
    private long totalRated;
    private Map<Integer, Long> distribution;
    private Double averageRating;
}
