package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.CreateDepartmentRequest;
import dev.nishants.appraisal.dtos.DepartmentResponse;
import dev.nishants.appraisal.entity.Department;
import dev.nishants.appraisal.exception.DuplicateResourceException;
import dev.nishants.appraisal.exception.ResourceNotFoundException;
import dev.nishants.appraisal.mappers.DepartmentMapper;
import dev.nishants.appraisal.repository.DepartmentRepository;
import dev.nishants.appraisal.services.DepartmentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

  private final DepartmentRepository departmentRepository;

  @Override
  @Transactional
  public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
    if (departmentRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException(
          "Department already exists with name: " + request.getName());
    }

    Department department = Department.builder()
        .name(request.getName())
        .description(request.getDescription())
        .build();

    departmentRepository.save(department);
    return DepartmentMapper.toResponse(department);
  }

  @Override
  @Transactional(readOnly = true)
  public DepartmentResponse getDepartmentById(Long id) {
    Department dept = departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    return DepartmentMapper.toResponse(dept);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DepartmentResponse> getAllDepartments() {
    return departmentRepository.findAll()
        .stream()
        .map(DepartmentMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request) {
    Department dept = departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Department", id));

    if (request.getName() != null)
      dept.setName(request.getName());
    if (request.getDescription() != null)
      dept.setDescription(request.getDescription());

    departmentRepository.save(dept);
    return DepartmentMapper.toResponse(dept);
  }

  @Override
  @Transactional
  public void deleteDepartment(Long id) {
    Department dept = departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    departmentRepository.delete(dept);
  }
}