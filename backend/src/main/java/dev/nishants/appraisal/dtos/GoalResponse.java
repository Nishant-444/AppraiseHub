package dev.nishants.appraisal.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import dev.nishants.appraisal.entity.Goal.Status;

@Getter
@Setter
public class GoalResponse {

    private Long id;
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String description;
    private Status status;
    private LocalDate dueDate;
}
