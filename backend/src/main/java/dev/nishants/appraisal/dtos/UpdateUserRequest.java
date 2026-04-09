package dev.nishants.appraisal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String fullName;
    private String jobTitle;
    private Long departmentId;
    private Long managerId;
    private Boolean isActive;
}
