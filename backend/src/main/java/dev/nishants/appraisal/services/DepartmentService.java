package dev.nishants.appraisal.services;


import java.util.List;

import dev.nishants.appraisal.dtos.CreateDepartmentRequest;
import dev.nishants.appraisal.dtos.DepartmentResponse;

public interface DepartmentService {

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse getDepartmentById(Long id);

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request);

    void deleteDepartment(Long id);
}
