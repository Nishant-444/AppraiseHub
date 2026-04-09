package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.AppraisalResponse;
import dev.nishants.appraisal.dtos.BulkCycleRequest;
import dev.nishants.appraisal.dtos.BulkCycleResponse;
import dev.nishants.appraisal.dtos.CreateAppraisalRequest;
import dev.nishants.appraisal.dtos.ManagerReviewRequest;
import dev.nishants.appraisal.dtos.SelfAssessmentRequest;
import dev.nishants.appraisal.entity.Appraisal;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.Notification.Type;
import dev.nishants.appraisal.entity.enums.AppraisalStatus;
import dev.nishants.appraisal.entity.enums.CycleStatus;
import dev.nishants.appraisal.entity.enums.Role;
import dev.nishants.appraisal.exception.InvalidStatusTransitionException;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;
import dev.nishants.appraisal.repository.AppraisalRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.AppraisalService;
import dev.nishants.appraisal.services.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppraisalServiceImpl implements AppraisalService {

  private final AppraisalRepository appraisalRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  // ── Create ────────────────────────────────────────────────────

  @Override
  public List<AppraisalResponse> getAllAppraisals() {
    requireHr(getCurrentUser());
    return appraisalRepository.findAllWithDetails()
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional
  public AppraisalResponse createAppraisal(CreateAppraisalRequest request) {
    if (appraisalRepository.existsByCycleNameAndEmployeeId(
        request.getCycleName(), request.getEmployeeId())) {
      throw new RuntimeException("Appraisal already exists for this employee in cycle: "
          + request.getCycleName());
    }

    User employee = findUserById(request.getEmployeeId());
    User manager = findUserById(request.getManagerId());

    if (manager.getRole() != Role.MANAGER)
      throw new RuntimeException("The assigned manager must have the MANAGER role");

    Appraisal appraisal = Appraisal.builder()
        .cycleName(request.getCycleName())
        .cycleStartDate(request.getCycleStartDate())
        .cycleEndDate(request.getCycleEndDate())
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(employee)
        .manager(manager)
        .appraisalStatus(AppraisalStatus.PENDING)
        .build();

    appraisalRepository.save(appraisal);

    notificationService.send(
        employee.getId(),
        "Appraisal cycle started",
        "Your appraisal for cycle '" + request.getCycleName()
            + "' has been created. Please submit your self-assessment.",
        Type.CYCLE_STARTED);

    return mapToResponse(appraisal);
  }

  @Override
  @Transactional
  public BulkCycleResponse createBulkCycle(BulkCycleRequest request) {
    // Fetch all active users who have a manager (both EMPLOYEE and MANAGER roles)
    List<User> employees = request.getDepartmentId() != null
        ? userRepository.findByDepartmentIdAndIsActiveTrue(request.getDepartmentId())
            .stream()
            .filter(u -> u.getRole() == Role.EMPLOYEE || u.getRole() == Role.MANAGER)
            .collect(Collectors.toList())
        : userRepository.findByIsActiveTrue()
            .stream()
            .filter(u -> u.getRole() == Role.EMPLOYEE || u.getRole() == Role.MANAGER)
            .collect(Collectors.toList());

    int created = 0, skippedAlreadyExists = 0, skippedNoManager = 0;

    for (User employee : employees) {
      if (employee.getManager() == null) {
        log.warn("Skipping employee {} (id={}) — no manager assigned",
            employee.getFullName(), employee.getId());
        skippedNoManager++;
        continue;
      }
      if (appraisalRepository.existsByCycleNameAndEmployeeId(
          request.getCycleName(), employee.getId())) {
        log.info("Skipping employee {} — appraisal already exists for cycle '{}'",
            employee.getFullName(), request.getCycleName());
        skippedAlreadyExists++;
        continue;
      }

      Appraisal appraisal = Appraisal.builder()
          .cycleName(request.getCycleName())
          .cycleStartDate(request.getCycleStartDate())
          .cycleEndDate(request.getCycleEndDate())
          .cycleStatus(CycleStatus.ACTIVE)
          .employee(employee)
          .manager(employee.getManager())
          .appraisalStatus(AppraisalStatus.PENDING)
          .build();

      appraisalRepository.save(appraisal);

      notificationService.send(
          employee.getId(),
          "Appraisal cycle started",
          "Your appraisal for cycle '" + request.getCycleName()
              + "' has been created. Please submit your self-assessment.",
          Type.CYCLE_STARTED);
      created++;
    }

    log.info("Bulk cycle '{}' — created: {}, skippedAlreadyExists: {}, skippedNoManager: {}",
        request.getCycleName(), created, skippedAlreadyExists, skippedNoManager);

    return new BulkCycleResponse(request.getCycleName(), employees.size(),
        created, skippedAlreadyExists, skippedNoManager);
  }

  // ── Read ──────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<AppraisalResponse> getMyAppraisals(Long employeeId) {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() != Role.HR && !currentUser.getId().equals(employeeId)) {
      throw new UnauthorizedAccessException("Access denied: you can only view your own appraisals");
    }
    return appraisalRepository.findByEmployeeId(employeeId)
        .stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppraisalResponse> getTeamAppraisals(Long managerId) {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() != Role.HR && !currentUser.getId().equals(managerId)) {
      throw new UnauthorizedAccessException("Access denied: you can only view your own team appraisals");
    }
    return appraisalRepository.findByManagerId(managerId)
        .stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId) {
    User currentUser = getCurrentUser();
    if (currentUser.getRole() != Role.HR && !currentUser.getId().equals(requesterId)) {
      throw new UnauthorizedAccessException("Access denied: requester mismatch");
    }
    Appraisal appraisal = findAppraisalById(appraisalId);
    boolean isEmployee = appraisal.getEmployee().getId().equals(requesterId);
    boolean isManager = appraisal.getManager().getId().equals(requesterId);
    if (currentUser.getRole() != Role.HR && !isEmployee && !isManager)
      throw new UnauthorizedAccessException("Access denied: you are not part of this appraisal");
    return mapToResponse(appraisal);
  }

  // ── Self-assessment draft ─────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse saveSelfAssessmentDraft(Long appraisalId,
      SelfAssessmentRequest request,
      Long employeeId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(employeeId)) {
      throw new UnauthorizedAccessException("Access denied: requester mismatch");
    }
    Appraisal appraisal = findAppraisalById(appraisalId);
    requireEmployee(appraisal, employeeId);

    AppraisalStatus status = appraisal.getAppraisalStatus();
    if (status != AppraisalStatus.PENDING && status != AppraisalStatus.EMPLOYEE_DRAFT) {
      throw new InvalidStatusTransitionException(
          "Cannot save draft. Self-assessment is already submitted. Current status: " + status);
    }

    applySelfAssessmentFields(appraisal, request);
    appraisal.setAppraisalStatus(AppraisalStatus.EMPLOYEE_DRAFT);
    appraisalRepository.save(appraisal);

    return mapToResponse(appraisal);
  }

  // ── Self-assessment submit ────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse submitSelfAssessment(Long appraisalId,
      SelfAssessmentRequest request,
      Long employeeId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(employeeId)) {
      throw new UnauthorizedAccessException("Access denied: requester mismatch");
    }
    Appraisal appraisal = findAppraisalById(appraisalId);
    requireEmployee(appraisal, employeeId);

    AppraisalStatus status = appraisal.getAppraisalStatus();
    if (status != AppraisalStatus.PENDING && status != AppraisalStatus.EMPLOYEE_DRAFT) {
      throw new InvalidStatusTransitionException(
          "Cannot submit self-assessment. Current status: " + status);
    }

    applySelfAssessmentFields(appraisal, request);
    appraisal.setAppraisalStatus(AppraisalStatus.SELF_SUBMITTED);
    appraisal.setSubmittedAt(LocalDateTime.now());
    appraisalRepository.save(appraisal);

    notificationService.send(
        appraisal.getManager().getId(),
        "Self-assessment submitted",
        appraisal.getEmployee().getFullName() + " has submitted their self-assessment for '"
            + appraisal.getCycleName() + "'. Please review and rate.",
        Type.SELF_ASSESSMENT_SUBMITTED);

    return mapToResponse(appraisal);
  }

  // ── Manager review draft ──────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse saveManagerReviewDraft(Long appraisalId,
      ManagerReviewRequest request,
      Long managerId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(managerId)) {
      throw new UnauthorizedAccessException("Access denied: requester mismatch");
    }
    Appraisal appraisal = findAppraisalById(appraisalId);
    requireManager(appraisal, managerId);

    AppraisalStatus status = appraisal.getAppraisalStatus();
    if (status != AppraisalStatus.SELF_SUBMITTED && status != AppraisalStatus.MANAGER_DRAFT) {
      throw new InvalidStatusTransitionException(
          "Cannot save manager draft. Current status: " + status);
    }

    applyManagerReviewFields(appraisal, request);
    appraisal.setAppraisalStatus(AppraisalStatus.MANAGER_DRAFT);
    appraisalRepository.save(appraisal);

    return mapToResponse(appraisal);
  }

  // ── Manager review submit ─────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse submitManagerReview(Long appraisalId,
      ManagerReviewRequest request,
      Long managerId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(managerId)) {
      throw new UnauthorizedAccessException("Access denied: requester mismatch");
    }
    Appraisal appraisal = findAppraisalById(appraisalId);
    requireManager(appraisal, managerId);

    AppraisalStatus status = appraisal.getAppraisalStatus();
    if (status != AppraisalStatus.SELF_SUBMITTED && status != AppraisalStatus.MANAGER_DRAFT) {
      throw new InvalidStatusTransitionException(
          "Cannot submit manager review. Current status: " + status);
    }

    applyManagerReviewFields(appraisal, request);
    appraisal.setAppraisalStatus(AppraisalStatus.MANAGER_REVIEWED);
    appraisalRepository.save(appraisal);

    // Notify all active HR users
    List<User> hrUsers = userRepository.findByRoleAndIsActiveTrue(Role.HR);
    for (User hr : hrUsers) {
      notificationService.send(
          hr.getId(),
          "Appraisal ready for approval",
          appraisal.getEmployee().getFullName() + "'s appraisal for '"
              + appraisal.getCycleName() + "' is ready for your approval.",
          Type.MANAGER_REVIEW_DONE);
    }

    // Notify the employee
    notificationService.send(
        appraisal.getEmployee().getId(),
        "Your appraisal has been reviewed",
        "Your manager has completed their review for '"
            + appraisal.getCycleName() + "'. Awaiting HR approval.",
        Type.MANAGER_REVIEW_DONE);

    return mapToResponse(appraisal);
  }

  // ── Approve ───────────────────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse approveAppraisal(Long appraisalId) {
    requireHr(getCurrentUser());
    Appraisal appraisal = findAppraisalById(appraisalId);

    if (appraisal.getAppraisalStatus() != AppraisalStatus.MANAGER_REVIEWED) {
      throw new InvalidStatusTransitionException(
          "Cannot approve. Current status: " + appraisal.getAppraisalStatus());
    }

    appraisal.setAppraisalStatus(AppraisalStatus.APPROVED);
    appraisal.setApprovedAt(LocalDateTime.now());
    appraisalRepository.save(appraisal);

    notificationService.send(
        appraisal.getEmployee().getId(),
        "Appraisal approved",
        "Your appraisal for '" + appraisal.getCycleName()
            + "' has been approved. Please review and acknowledge.",
        Type.APPRAISAL_APPROVED);

    return mapToResponse(appraisal);
  }

  // ── Acknowledge ───────────────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId) {
    User currentUser = getCurrentUser();
    if (!currentUser.getId().equals(employeeId)) {
      throw new UnauthorizedAccessException("Access denied: requester mismatch");
    }
    Appraisal appraisal = findAppraisalById(appraisalId);
    requireEmployee(appraisal, employeeId);

    if (appraisal.getAppraisalStatus() != AppraisalStatus.APPROVED) {
      throw new InvalidStatusTransitionException(
          "Cannot acknowledge. Current status: " + appraisal.getAppraisalStatus());
    }

    appraisal.setAppraisalStatus(AppraisalStatus.ACKNOWLEDGED);
    appraisalRepository.save(appraisal);

    return mapToResponse(appraisal);
  }

  // ── Private helpers ───────────────────────────────────────────

  private void requireEmployee(Appraisal appraisal, Long employeeId) {
    if (!appraisal.getEmployee().getId().equals(employeeId))
      throw new UnauthorizedAccessException("Access denied: this is not your appraisal");
  }

  private void requireManager(Appraisal appraisal, Long managerId) {
    if (!appraisal.getManager().getId().equals(managerId))
      throw new UnauthorizedAccessException("Access denied: you are not the manager for this appraisal");
  }

  private User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmailWithDetails(email)
        .orElseThrow(() -> new UnauthorizedAccessException("Access denied: user not found"));
  }

  private void requireHr(User user) {
    if (user.getRole() != Role.HR) {
      throw new UnauthorizedAccessException("Access denied: HR role required");
    }
  }

  private void applySelfAssessmentFields(Appraisal appraisal, SelfAssessmentRequest request) {
    appraisal.setWhatWentWell(request.getWhatWentWell());
    appraisal.setWhatToImprove(request.getWhatToImprove());
    appraisal.setAchievements(request.getAchievements());
    appraisal.setSelfRating(request.getSelfRating());
  }

  private void applyManagerReviewFields(Appraisal appraisal, ManagerReviewRequest request) {
    appraisal.setManagerStrengths(request.getManagerStrengths());
    appraisal.setManagerImprovements(request.getManagerImprovements());
    appraisal.setManagerComments(request.getManagerComments());
    appraisal.setManagerRating(request.getManagerRating());
  }

  private Appraisal findAppraisalById(Long id) {
    return appraisalRepository.findByIdWithDetails(id)
        .orElseThrow(() -> new RuntimeException("Appraisal not found with id: " + id));
  }

  private User findUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
  }

  private AppraisalResponse mapToResponse(Appraisal appraisal) {
    AppraisalResponse response = new AppraisalResponse();
    response.setId(appraisal.getId());
    response.setCycleName(appraisal.getCycleName());
    response.setCycleStartDate(appraisal.getCycleStartDate());
    response.setCycleEndDate(appraisal.getCycleEndDate());
    response.setCycleStatus(appraisal.getCycleStatus());
    response.setEmployeeId(appraisal.getEmployee().getId());
    response.setEmployeeName(appraisal.getEmployee().getFullName());
    response.setEmployeeJobTitle(appraisal.getEmployee().getJobTitle());
    if (appraisal.getEmployee().getDepartment() != null)
      response.setEmployeeDepartment(appraisal.getEmployee().getDepartment().getName());
    response.setManagerId(appraisal.getManager().getId());
    response.setManagerName(appraisal.getManager().getFullName());
    response.setWhatWentWell(appraisal.getWhatWentWell());
    response.setWhatToImprove(appraisal.getWhatToImprove());
    response.setAchievements(appraisal.getAchievements());
    response.setSelfRating(appraisal.getSelfRating());
    response.setManagerStrengths(appraisal.getManagerStrengths());
    response.setManagerImprovements(appraisal.getManagerImprovements());
    response.setManagerComments(appraisal.getManagerComments());
    response.setManagerRating(appraisal.getManagerRating());
    response.setAppraisalStatus(appraisal.getAppraisalStatus());
    response.setSubmittedAt(appraisal.getSubmittedAt());
    response.setApprovedAt(appraisal.getApprovedAt());
    response.setCreatedAt(appraisal.getCreatedAt());
    return response;
  }

}
