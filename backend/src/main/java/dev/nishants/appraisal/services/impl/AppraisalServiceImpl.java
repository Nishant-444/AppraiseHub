package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.AppraisalResponse;
import dev.nishants.appraisal.dtos.BulkCycleRequest;
import dev.nishants.appraisal.dtos.BulkCycleResponse;
import dev.nishants.appraisal.dtos.CreateAppraisalRequest;
import dev.nishants.appraisal.dtos.ManagerReviewRequest;
import dev.nishants.appraisal.dtos.NotificationPayload;
import dev.nishants.appraisal.dtos.SelfAssessmentRequest;
import dev.nishants.appraisal.entity.Appraisal;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.Notification.Type;
import dev.nishants.appraisal.entity.enums.AppraisalStatus;
import dev.nishants.appraisal.entity.enums.CycleStatus;
import dev.nishants.appraisal.entity.enums.Role;
import dev.nishants.appraisal.exception.DuplicateResourceException;
import dev.nishants.appraisal.exception.InvalidStatusTransitionException;
import dev.nishants.appraisal.exception.UnauthorizedAccessException;
import dev.nishants.appraisal.mappers.AppraisalMapper;
import dev.nishants.appraisal.repository.AppraisalRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.AppraisalService;
import dev.nishants.appraisal.services.AuthorizationService;
import dev.nishants.appraisal.services.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppraisalServiceImpl implements AppraisalService {

  private final AppraisalRepository appraisalRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final AuthorizationService authorizationService;

  // ── Create ────────────────────────────────────────────────────

  @Override
  public List<AppraisalResponse> getAllAppraisals() {
    authorizationService.requireHr();
    return appraisalRepository.findAllWithDetails()
        .stream()
        .map(AppraisalMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public AppraisalResponse createAppraisal(CreateAppraisalRequest request) {
    validateCycleYear(request.getCycleName(), request.getCycleStartDate(), request.getCycleEndDate());

    LocalDate yearStart = getYearStart(request.getCycleStartDate());
    LocalDate yearEnd = getYearEnd(request.getCycleStartDate());
    if (appraisalRepository.existsByEmployeeIdAndCycleStartDateBetween(
        request.getEmployeeId(), yearStart, yearEnd)) {
      throw new DuplicateResourceException("Employee already has an appraisal cycle for year "
          + request.getCycleStartDate().getYear());
    }

    if (appraisalRepository.existsByCycleNameAndEmployeeId(
        request.getCycleName(), request.getEmployeeId())) {
      throw new DuplicateResourceException("Appraisal already exists for this employee in cycle: "
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
        Type.CYCLE_STARTED,
        NotificationPayload.forCycle(
            request.getCycleName(),
            request.getCycleStartDate().toString(),
            request.getCycleEndDate().toString()));

    return AppraisalMapper.toResponse(appraisal);
  }

  @Override
  @Transactional
  public BulkCycleResponse createBulkCycle(BulkCycleRequest request) {
    validateCycleYear(request.getCycleName(), request.getCycleStartDate(), request.getCycleEndDate());
    LocalDate yearStart = getYearStart(request.getCycleStartDate());
    LocalDate yearEnd = getYearEnd(request.getCycleStartDate());

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
        skippedNoManager++;
        continue;
      }
      if (appraisalRepository.existsByEmployeeIdAndCycleStartDateBetween(
          employee.getId(), yearStart, yearEnd)) {
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
          Type.CYCLE_STARTED,
          NotificationPayload.forCycle(
              request.getCycleName(),
              request.getCycleStartDate().toString(),
              request.getCycleEndDate().toString()));
      created++;
    }

    return new BulkCycleResponse(request.getCycleName(), employees.size(),
        created, skippedAlreadyExists, skippedNoManager);
  }

  private void validateCycleYear(String cycleName, LocalDate startDate, LocalDate endDate) {
    LocalDate yearStart = getYearStart(startDate);
    LocalDate yearEnd = getYearEnd(startDate);
    List<String> existingCycleNames = appraisalRepository
        .findDistinctCycleNamesInYear(yearStart, yearEnd);

    if (!existingCycleNames.isEmpty() && !existingCycleNames.contains(cycleName)) {
      throw new DuplicateResourceException("An appraisal cycle already exists for year "
          + startDate.getYear() + ": " + existingCycleNames.get(0));
    }

    if (existingCycleNames.size() > 1) {
      throw new DuplicateResourceException("Multiple appraisal cycles already exist for year "
          + startDate.getYear() + ". Please resolve before creating a new cycle.");
    }

    appraisalRepository.findFirstByCycleNameAndCycleStartDateBetween(
        cycleName, yearStart, yearEnd)
        .ifPresent(existing -> {
          if (!existing.getCycleStartDate().equals(startDate)
              || !existing.getCycleEndDate().equals(endDate)) {
            throw new DuplicateResourceException("Cycle dates must match existing cycle '" +
                cycleName + "'.");
          }
        });
  }

  private LocalDate getYearStart(LocalDate cycleStartDate) {
    return LocalDate.of(cycleStartDate.getYear(), 1, 1);
  }

  private LocalDate getYearEnd(LocalDate cycleStartDate) {
    return LocalDate.of(cycleStartDate.getYear(), 12, 31);
  }

  // ── Read ──────────────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<AppraisalResponse> getMyAppraisals(Long employeeId) {
    authorizationService.requireSelfOrHr(
        employeeId,
        "Access denied: you can only view your own appraisals");
    return appraisalRepository.findByEmployeeId(employeeId)
        .stream().map(AppraisalMapper::toResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppraisalResponse> getTeamAppraisals(Long managerId) {
    authorizationService.requireSelfOrHr(
        managerId,
        "Access denied: you can only view your own team appraisals");
    return appraisalRepository.findByManagerId(managerId)
        .stream().map(AppraisalMapper::toResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId) {
    User currentUser = authorizationService.requireSelfOrHr(
        requesterId,
        "Access denied: requester mismatch");
    Appraisal appraisal = findAppraisalById(appraisalId);
    boolean isEmployee = appraisal.getEmployee().getId().equals(requesterId);
    boolean isManager = appraisal.getManager().getId().equals(requesterId);
    if (!authorizationService.isHr(currentUser) && !isEmployee && !isManager)
      throw new UnauthorizedAccessException("Access denied: you are not part of this appraisal");
    return AppraisalMapper.toResponse(appraisal);
  }

  // ── Self-assessment draft ─────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse saveSelfAssessmentDraft(Long appraisalId,
      SelfAssessmentRequest request,
      Long employeeId) {
    authorizationService.requireSelf(employeeId, "Access denied: requester mismatch");
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

    return AppraisalMapper.toResponse(appraisal);
  }

  // ── Self-assessment submit ────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse submitSelfAssessment(Long appraisalId,
      SelfAssessmentRequest request,
      Long employeeId) {
    authorizationService.requireSelf(employeeId, "Access denied: requester mismatch");
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
        Type.SELF_ASSESSMENT_SUBMITTED,
        NotificationPayload.forEmployeeCycle(
            appraisal.getEmployee().getFullName(),
            appraisal.getCycleName()));

    return AppraisalMapper.toResponse(appraisal);
  }

  // ── Manager review draft ──────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse saveManagerReviewDraft(Long appraisalId,
      ManagerReviewRequest request,
      Long managerId) {
    authorizationService.requireSelf(managerId, "Access denied: requester mismatch");
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

    return AppraisalMapper.toResponse(appraisal);
  }

  // ── Manager review submit ─────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse submitManagerReview(Long appraisalId,
      ManagerReviewRequest request,
      Long managerId) {
    authorizationService.requireSelf(managerId, "Access denied: requester mismatch");
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
          Type.MANAGER_REVIEW_DONE,
          NotificationPayload.forCycleName(appraisal.getCycleName()));
    }

    // Notify the employee
    notificationService.send(
        appraisal.getEmployee().getId(),
        "Your appraisal has been reviewed",
        "Your manager has completed their review for '"
            + appraisal.getCycleName() + "'. Awaiting HR approval.",
        Type.MANAGER_REVIEW_DONE,
        NotificationPayload.forCycleName(appraisal.getCycleName()));

    return AppraisalMapper.toResponse(appraisal);
  }

  // ── Approve ───────────────────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse approveAppraisal(Long appraisalId) {
    authorizationService.requireHr();
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
        Type.APPRAISAL_APPROVED,
        NotificationPayload.forCycleName(appraisal.getCycleName()));

    return AppraisalMapper.toResponse(appraisal);
  }

  // ── Acknowledge ───────────────────────────────────────────────

  @Override
  @Transactional
  public AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId) {
    authorizationService.requireSelf(employeeId, "Access denied: requester mismatch");
    Appraisal appraisal = findAppraisalById(appraisalId);
    requireEmployee(appraisal, employeeId);

    if (appraisal.getAppraisalStatus() != AppraisalStatus.APPROVED) {
      throw new InvalidStatusTransitionException(
          "Cannot acknowledge. Current status: " + appraisal.getAppraisalStatus());
    }

    appraisal.setAppraisalStatus(AppraisalStatus.ACKNOWLEDGED);
    appraisalRepository.save(appraisal);

    return AppraisalMapper.toResponse(appraisal);
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
}
