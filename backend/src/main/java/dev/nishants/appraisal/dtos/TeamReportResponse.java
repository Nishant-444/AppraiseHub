package dev.nishants.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

import dev.nishants.appraisal.entity.enums.AppraisalStatus;

@Getter
@Builder
public class TeamReportResponse {
    private String cycleName;
    private String managerName;
    private int totalTeamMembers;
    private Double teamAverageRating;
    private List<TeamMemberReport> members;

    @Getter
    @Builder
    public static class TeamMemberReport {
        private Long employeeId;
        private String employeeName;
        private String jobTitle;
        private Integer selfRating;
        private Integer managerRating;
        private AppraisalStatus status;
        private long goalsCompleted;
        private long totalGoals;
    }
}
