package dev.nishants.appraisal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.nishants.appraisal.dtos.ApiResponse;
import dev.nishants.appraisal.dtos.CreateDepartmentRequest;
import dev.nishants.appraisal.dtos.DepartmentResponse;
import dev.nishants.appraisal.services.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // POST /api/departments
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {

        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", response));
    }

    // GET /api/departments
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllDepartments()));
    }

    // GET /api/departments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartmentById(id)));
    }

    // PUT /api/departments/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody CreateDepartmentRequest request) {

        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", response));
    }

    // DELETE /api/departments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
    }
}
