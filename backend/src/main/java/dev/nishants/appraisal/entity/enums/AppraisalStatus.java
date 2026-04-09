package dev.nishants.appraisal.entity.enums;

public enum AppraisalStatus {
    PENDING,
    EMPLOYEE_DRAFT,      // employee saved progress but not yet submitted
    SELF_SUBMITTED,
    MANAGER_DRAFT,       // manager saved progress but not yet submitted
    MANAGER_REVIEWED,
    APPROVED,
    ACKNOWLEDGED
}
