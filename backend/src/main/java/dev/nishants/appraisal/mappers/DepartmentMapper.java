package dev.nishants.appraisal.mappers;

import dev.nishants.appraisal.dtos.DepartmentResponse;
import dev.nishants.appraisal.entity.Department;

public final class DepartmentMapper {

  private DepartmentMapper() {
  }

  public static DepartmentResponse toResponse(Department department) {
    DepartmentResponse response = new DepartmentResponse();
    response.setId(department.getId());
    response.setName(department.getName());
    response.setDescription(department.getDescription());
    return response;
  }
}
