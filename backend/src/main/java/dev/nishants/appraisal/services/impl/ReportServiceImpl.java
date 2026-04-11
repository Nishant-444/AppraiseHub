package dev.nishants.appraisal.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.dtos.*;
import dev.nishants.appraisal.entity.Appraisal;
import dev.nishants.appraisal.entity.Department;
import dev.nishants.appraisal.entity.Goal;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.enums.AppraisalStatus;
import dev.nishants.appraisal.exception.ResourceNotFoundException;
import dev.nishants.appraisal.repository.AppraisalRepository;
import dev.nishants.appraisal.repository.DepartmentRepository;
import dev.nishants.appraisal.repository.GoalRepository;
import dev.nishants.appraisal.repository.UserRepository;
import dev.nishants.appraisal.services.AuthorizationService;
import dev.nishants.appraisal.services.ReportService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

  private final AppraisalRepository appraisalRepository;
  private final GoalRepository goalRepository;
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final AuthorizationService authorizationService;

  // ── Cycle Summary ─────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public CycleSummaryResponse getCycleSummary(String cycleName) {
    authorizationService.requireHr();
    List<Object[]> rows = appraisalRepository.countByStatusForCycle(cycleName);

    long pending = 0, employeeDraft = 0, selfSubmitted = 0,
        managerDraft = 0, managerReviewed = 0, approved = 0, acknowledged = 0;

    for (Object[] row : rows) {
      AppraisalStatus status = (AppraisalStatus) row[0];
      long count = (Long) row[1];
      switch (status) {
        case PENDING -> pending = count;
        case EMPLOYEE_DRAFT -> employeeDraft = count;
        case SELF_SUBMITTED -> selfSubmitted = count;
        case MANAGER_DRAFT -> managerDraft = count;
        case MANAGER_REVIEWED -> managerReviewed = count;
        case APPROVED -> approved = count;
        case ACKNOWLEDGED -> acknowledged = count;
      }
    }

    long total = pending + employeeDraft + selfSubmitted + managerDraft
        + managerReviewed + approved + acknowledged;

    double completionPct = total == 0 ? 0.0
        : Math.round(((approved + acknowledged) * 100.0 / total) * 10.0) / 10.0;

    Double avgRating = appraisalRepository.averageManagerRatingForCycle(cycleName);

    return CycleSummaryResponse.builder()
        .cycleName(cycleName)
        .totalAppraisals(total)
        .pending(pending)
        .employeeDraft(employeeDraft)
        .selfSubmitted(selfSubmitted)
        .managerDraft(managerDraft)
        .managerReviewed(managerReviewed)
        .approved(approved)
        .acknowledged(acknowledged)
        .completionPercentage(completionPct)
        .averageManagerRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null)
        .build();
  }

  // ── Department Report ─────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public List<DepartmentReportResponse> getDepartmentReport(String cycleName) {
    authorizationService.requireHr();
    List<Department> departments = departmentRepository.findAll();
    List<Appraisal> cycleAppraisals = appraisalRepository.findByCycleName(cycleName);

    // Index appraisals by employee id for fast lookup
    Map<Long, Appraisal> appraisalByEmployee = cycleAppraisals.stream()
        .collect(Collectors.toMap(a -> a.getEmployee().getId(), a -> a, (a, b) -> a));

    List<DepartmentReportResponse> result = new ArrayList<>();

    for (Department dept : departments) {
      List<User> employees = userRepository.findByDepartmentIdAndIsActiveTrue(dept.getId());
      if (employees.isEmpty())
        continue;

      long completed = 0;
      long pending = 0;
      double ratingSum = 0;
      int ratingCount = 0;

      for (User emp : employees) {
        Appraisal a = appraisalByEmployee.get(emp.getId());
        if (a == null) {
          pending++;
          continue;
        }
        if (a.getAppraisalStatus() == AppraisalStatus.APPROVED
            || a.getAppraisalStatus() == AppraisalStatus.ACKNOWLEDGED) {
          completed++;
          if (a.getManagerRating() != null) {
            ratingSum += a.getManagerRating();
            ratingCount++;
          }
        } else {
          pending++;
        }
      }

      Double avgRating = ratingCount > 0
          ? Math.round((ratingSum / ratingCount) * 10.0) / 10.0
          : null;

      result.add(DepartmentReportResponse.builder()
          .departmentName(dept.getName())
          .totalEmployees(employees.size())
          .completed(completed)
          .pending(pending)
          .averageRating(avgRating)
          .build());
    }

    return result;
  }

  // ── Rating Distribution ───────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public RatingDistributionResponse getRatingDistribution(String cycleName) {
    authorizationService.requireHr();
    List<Object[]> rows = appraisalRepository.getRatingDistribution(cycleName);

    Map<Integer, Long> distribution = new LinkedHashMap<>();
    // Pre-fill 1-5 with 0 so all keys are always present
    for (int i = 1; i <= 5; i++)
      distribution.put(i, 0L);

    long total = 0;
    for (Object[] row : rows) {
      int rating = (Integer) row[0];
      long count = (Long) row[1];
      distribution.put(rating, count);
      total += count;
    }

    Double avg = appraisalRepository.averageManagerRatingForCycle(cycleName);

    return RatingDistributionResponse.builder()
        .cycleName(cycleName)
        .totalRated(total)
        .distribution(distribution)
        .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
        .build();
  }

  // ── Pending Report ────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public PendingReportResponse getPendingReport(String cycleName) {
    authorizationService.requireHr();
    List<Appraisal> pending = appraisalRepository.findPendingAppraisalsForCycle(cycleName);

    List<PendingReportResponse.PendingEntry> entries = pending.stream()
        .map(a -> PendingReportResponse.PendingEntry.builder()
            .employeeId(a.getEmployee().getId())
            .employeeName(a.getEmployee().getFullName())
            .managerName(a.getManager().getFullName())
            .departmentName(a.getEmployee().getDepartment() != null
                ? a.getEmployee().getDepartment().getName()
                : null)
            .currentStatus(a.getAppraisalStatus())
            .build())
        .collect(Collectors.toList());

    return PendingReportResponse.builder()
        .cycleName(cycleName)
        .totalPending(entries.size())
        .entries(entries)
        .build();
  }

  // ── Team Report ───────────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public TeamReportResponse getTeamReport(String cycleName, Long managerId) {
    authorizationService.requireSelfOrHr(
        managerId,
        "Access denied: you can only view your own team report");
    List<Appraisal> appraisals = appraisalRepository.findTeamAppraisalsForCycle(cycleName, managerId);
    Double teamAvg = appraisalRepository.averageRatingForTeam(cycleName, managerId);

    String managerName = appraisals.isEmpty() ? "" : appraisals.get(0).getManager().getFullName();

    List<TeamReportResponse.TeamMemberReport> members = appraisals.stream()
        .map(a -> {
          long totalGoals = goalRepository.countByAppraisalId(a.getId());
          long goalsCompleted = goalRepository.countByAppraisalIdAndStatus(a.getId(), Goal.Status.COMPLETED);
          return TeamReportResponse.TeamMemberReport.builder()
              .employeeId(a.getEmployee().getId())
              .employeeName(a.getEmployee().getFullName())
              .jobTitle(a.getEmployee().getJobTitle())
              .selfRating(a.getSelfRating())
              .managerRating(a.getManagerRating())
              .status(a.getAppraisalStatus())
              .goalsCompleted(goalsCompleted)
              .totalGoals(totalGoals)
              .build();
        })
        .collect(Collectors.toList());

    return TeamReportResponse.builder()
        .cycleName(cycleName)
        .managerName(managerName)
        .totalTeamMembers(members.size())
        .teamAverageRating(teamAvg != null ? Math.round(teamAvg * 10.0) / 10.0 : null)
        .members(members)
        .build();
  }

  // ── Employee History ──────────────────────────────────────────

  @Override
  @Transactional(readOnly = true)
  public EmployeeHistoryResponse getEmployeeHistory(Long employeeId) {
    authorizationService.requireSelfOrHr(
        employeeId,
        "Access denied: you can only view your own history");
    List<Appraisal> history = appraisalRepository.findEmployeeHistory(employeeId);

    String employeeName = history.isEmpty()
        ? userRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("User", employeeId))
            .getFullName()
        : history.get(0).getEmployee().getFullName();

    List<EmployeeHistoryResponse.CycleRecord> cycles = history.stream()
        .map(a -> EmployeeHistoryResponse.CycleRecord.builder()
            .cycleName(a.getCycleName())
            .cycleStartDate(a.getCycleStartDate())
            .cycleEndDate(a.getCycleEndDate())
            .selfRating(a.getSelfRating())
            .managerRating(a.getManagerRating())
            .status(a.getAppraisalStatus())
            .managerName(a.getManager().getFullName())
            .build())
        .collect(Collectors.toList());

    return EmployeeHistoryResponse.builder()
        .employeeId(employeeId)
        .employeeName(employeeName)
        .cycles(cycles)
        .build();
  }

}
