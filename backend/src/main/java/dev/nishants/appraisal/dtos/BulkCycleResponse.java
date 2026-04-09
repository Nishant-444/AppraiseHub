package dev.nishants.appraisal.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BulkCycleResponse {

    private String cycleName;
    private int totalEmployees;
    private int created;
    private int skippedAlreadyExists;
    private int skippedNoManager;
}
