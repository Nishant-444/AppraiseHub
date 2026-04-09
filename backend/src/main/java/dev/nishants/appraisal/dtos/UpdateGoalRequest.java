package dev.nishants.appraisal.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateGoalRequest {

    private String title;
    private String description;
    private LocalDate dueDate;
}
