package dev.nishants.appraisal.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.nishants.appraisal.entity.Appraisal;
import dev.nishants.appraisal.entity.Department;
import dev.nishants.appraisal.entity.Goal;
import dev.nishants.appraisal.entity.Notification;
import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.enums.AppraisalStatus;
import dev.nishants.appraisal.entity.enums.CycleStatus;
import dev.nishants.appraisal.entity.enums.Role;
import dev.nishants.appraisal.repository.AppraisalRepository;
import dev.nishants.appraisal.repository.DepartmentRepository;
import dev.nishants.appraisal.repository.GoalRepository;
import dev.nishants.appraisal.repository.NotificationRepository;
import dev.nishants.appraisal.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;
  private final AppraisalRepository appraisalRepository;
  private final GoalRepository goalRepository;
  private final NotificationRepository notificationRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {
    if (userRepository.count() > 0) {
      return;
    }

    String defaultPassword = passwordEncoder.encode("Password123!");

    Department engineering = departmentRepository.save(Department.builder()
        .name("Engineering")
        .description("Builds and maintains the AppraiseHub platform")
        .build());

    Department sales = departmentRepository.save(Department.builder()
        .name("Sales")
        .description("Drives revenue and customer relationships")
        .build());

    Department peopleOps = departmentRepository.save(Department.builder()
        .name("People Ops")
        .description("Supports hiring, onboarding, and employee success")
        .build());

    Department finance = departmentRepository.save(Department.builder()
        .name("Finance")
        .description("Owns budgeting, payroll, and financial planning")
        .build());

    Department leadership = departmentRepository.save(Department.builder()
        .name("Leadership")
        .description("Executive leadership and strategic alignment")
        .build());

    User hrLead = userRepository.save(User.builder()
        .fullName("Renee Patel")
        .email("hr@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.HR)
        .jobTitle("HR Lead")
        .department(peopleOps)
        .isActive(true)
        .build());

    User opsVp = userRepository.save(User.builder()
        .fullName("Casey Roberts")
        .email("casey.roberts@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.MANAGER)
        .jobTitle("VP of Operations")
        .department(leadership)
        .isActive(true)
        .build());

    User engManager = userRepository.save(User.builder()
        .fullName("Jordan Lee")
        .email("jordan.lee@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.MANAGER)
        .jobTitle("Engineering Manager")
        .department(engineering)
        .manager(opsVp)
        .isActive(true)
        .build());

    User salesManager = userRepository.save(User.builder()
        .fullName("Avery Nguyen")
        .email("avery.nguyen@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.MANAGER)
        .jobTitle("Sales Manager")
        .department(sales)
        .manager(opsVp)
        .isActive(true)
        .build());

    User financeManager = userRepository.save(User.builder()
        .fullName("Priya Shah")
        .email("priya.shah@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.MANAGER)
        .jobTitle("Finance Manager")
        .department(finance)
        .manager(opsVp)
        .isActive(true)
        .build());

    User eng1 = userRepository.save(User.builder()
        .fullName("Maya Torres")
        .email("maya.torres@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.EMPLOYEE)
        .jobTitle("Senior Software Engineer")
        .department(engineering)
        .manager(engManager)
        .isActive(true)
        .build());

    User eng2 = userRepository.save(User.builder()
        .fullName("Noah Brooks")
        .email("noah.brooks@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.EMPLOYEE)
        .jobTitle("Backend Engineer")
        .department(engineering)
        .manager(engManager)
        .isActive(true)
        .build());

    User eng3 = userRepository.save(User.builder()
        .fullName("Kira Morales")
        .email("kira.morales@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.EMPLOYEE)
        .jobTitle("Product Engineer")
        .department(engineering)
        .manager(engManager)
        .isActive(true)
        .build());

    User sales1 = userRepository.save(User.builder()
        .fullName("Elliot Price")
        .email("elliot.price@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.EMPLOYEE)
        .jobTitle("Account Executive")
        .department(sales)
        .manager(salesManager)
        .isActive(true)
        .build());

    User sales2 = userRepository.save(User.builder()
        .fullName("Sofia Kim")
        .email("sofia.kim@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.EMPLOYEE)
        .jobTitle("Sales Development Rep")
        .department(sales)
        .manager(salesManager)
        .isActive(true)
        .build());

    User finance1 = userRepository.save(User.builder()
        .fullName("Liam Osei")
        .email("liam.osei@AppraiseHub.io")
        .password(defaultPassword)
        .role(Role.EMPLOYEE)
        .jobTitle("Financial Analyst")
        .department(finance)
        .manager(financeManager)
        .isActive(true)
        .build());

    LocalDate h2Start = LocalDate.of(2025, 7, 1);
    LocalDate h2End = LocalDate.of(2025, 12, 31);
    LocalDate h1Start = LocalDate.of(2026, 1, 1);
    LocalDate h1End = LocalDate.of(2026, 6, 30);

    appraisalRepository.save(Appraisal.builder()
        .cycleName("2025 H2")
        .cycleStartDate(h2Start)
        .cycleEndDate(h2End)
        .cycleStatus(CycleStatus.CLOSED)
        .employee(eng1)
        .manager(engManager)
        .whatWentWell("Delivered the performance review workflow and mentored two interns.")
        .whatToImprove("Reduce review cycle time by automating more QA tasks.")
        .achievements("Migrated core APIs to async processing and improved latency 18%.")
        .selfRating(4)
        .managerStrengths("Consistent delivery, strong collaboration across squads.")
        .managerImprovements("Keep writing more design docs earlier in the cycle.")
        .managerComments("Great impact on core platform reliability.")
        .managerRating(4)
        .appraisalStatus(AppraisalStatus.ACKNOWLEDGED)
        .submittedAt(LocalDateTime.of(2025, 10, 3, 10, 30))
        .approvedAt(LocalDateTime.of(2025, 10, 20, 14, 0))
        .build());

    Appraisal eng1H1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(eng1)
        .manager(engManager)
        .whatWentWell("Shipped the new goal tracker and paired with design for usability fixes.")
        .whatToImprove("Delegate more to accelerate delivery during peak weeks.")
        .achievements("Closed 3 high priority customer escalations and led postmortems.")
        .selfRating(5)
        .appraisalStatus(AppraisalStatus.SELF_SUBMITTED)
        .submittedAt(LocalDateTime.of(2026, 3, 14, 9, 15))
        .build());

    Appraisal eng2H1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(eng2)
        .manager(engManager)
        .whatWentWell("Improved build stability and added service health dashboards.")
        .whatToImprove("Clarify requirements earlier with PM before implementation.")
        .achievements("Introduced database backup automation and reduced alert noise 25%.")
        .selfRating(4)
        .appraisalStatus(AppraisalStatus.EMPLOYEE_DRAFT)
        .build());

    Appraisal eng3H1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(eng3)
        .manager(engManager)
        .appraisalStatus(AppraisalStatus.PENDING)
        .build());

    Appraisal engManagerH1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(engManager)
        .manager(opsVp)
        .whatWentWell("Aligned roadmap execution and reduced incident backlog.")
        .whatToImprove("Increase coaching touchpoints with senior engineers.")
        .achievements("Hired 3 engineers and shipped performance improvements.")
        .selfRating(4)
        .appraisalStatus(AppraisalStatus.SELF_SUBMITTED)
        .submittedAt(LocalDateTime.of(2026, 3, 18, 10, 0))
        .build());

    Appraisal sales1H1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(sales1)
        .manager(salesManager)
        .managerStrengths("Consistent pipeline hygiene and strong customer feedback.")
        .managerImprovements("Focus on upsell motion during renewal season.")
        .managerComments("Exceeded quota for two consecutive quarters.")
        .managerRating(5)
        .appraisalStatus(AppraisalStatus.MANAGER_REVIEWED)
        .submittedAt(LocalDateTime.of(2026, 2, 20, 16, 45))
        .build());

    Appraisal sales2H1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(sales2)
        .manager(salesManager)
        .whatWentWell("Booked 6 new demos and partnered with marketing for a webinar.")
        .whatToImprove("Improve follow-up cadence on mid-funnel opportunities.")
        .achievements("Generated 320k in qualified pipeline.")
        .selfRating(4)
        .appraisalStatus(AppraisalStatus.SELF_SUBMITTED)
        .submittedAt(LocalDateTime.of(2026, 3, 2, 11, 0))
        .build());

    Appraisal salesManagerH1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(salesManager)
        .manager(opsVp)
        .managerStrengths("Strong pipeline coverage and coaching of new reps.")
        .managerImprovements("Create tighter weekly forecast cadence.")
        .managerComments("Solid leadership through a busy quarter.")
        .managerRating(4)
        .appraisalStatus(AppraisalStatus.MANAGER_REVIEWED)
        .submittedAt(LocalDateTime.of(2026, 2, 26, 15, 30))
        .build());

    Appraisal finance1H1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(finance1)
        .manager(financeManager)
        .whatWentWell("Introduced quarterly forecast model and reduced variance.")
        .whatToImprove("Build closer relationships with ops to align on headcount planning.")
        .achievements("Completed SOC2 evidence gathering for finance controls.")
        .selfRating(4)
        .managerStrengths("Reliable stakeholder updates and strong analytical skills.")
        .managerImprovements("Present insights earlier during exec reviews.")
        .managerComments("Strong progress; keep driving automation.")
        .managerRating(4)
        .appraisalStatus(AppraisalStatus.APPROVED)
        .submittedAt(LocalDateTime.of(2026, 2, 5, 9, 0))
        .approvedAt(LocalDateTime.of(2026, 2, 18, 13, 30))
        .build());

    Appraisal financeManagerH1 = appraisalRepository.save(Appraisal.builder()
        .cycleName("2026 H1")
        .cycleStartDate(h1Start)
        .cycleEndDate(h1End)
        .cycleStatus(CycleStatus.ACTIVE)
        .employee(financeManager)
        .manager(opsVp)
        .whatWentWell("Improved forecasting accuracy and stakeholder visibility.")
        .whatToImprove("Drive more automation in month-end close.")
        .achievements("Implemented new budget review cadence across departments.")
        .selfRating(4)
        .appraisalStatus(AppraisalStatus.SELF_SUBMITTED)
        .submittedAt(LocalDateTime.of(2026, 3, 6, 9, 45))
        .build());

    goalRepository.saveAll(List.of(
        Goal.builder()
            .appraisal(eng1H1)
            .employee(eng1)
            .title("Reduce API error rate below 0.3%")
            .description("Ship rate limiting improvements and add circuit breakers.")
            .progressPercent(70)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 5, 30))
            .build(),
        Goal.builder()
            .appraisal(eng1H1)
            .employee(eng1)
            .title("Deliver onboarding revamp")
            .description("Partner with People Ops to update engineering onboarding docs.")
            .progressPercent(40)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 6, 15))
            .build(),
        Goal.builder()
            .appraisal(eng2H1)
            .employee(eng2)
            .title("Stabilize nightly build")
            .description("Reduce flaky tests and improve CI caching.")
            .progressPercent(55)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 4, 30))
            .build(),
        Goal.builder()
            .appraisal(eng3H1)
            .employee(eng3)
            .title("Ship customer analytics dashboard")
            .description("Deliver MVP and iterate with design feedback.")
            .progressPercent(0)
            .status(Goal.Status.NOT_STARTED)
            .dueDate(LocalDate.of(2026, 6, 10))
            .build(),
        Goal.builder()
            .appraisal(sales1H1)
            .employee(sales1)
            .title("Close 1.2M in new ARR")
            .description("Focus on mid-market SaaS accounts and renewals.")
            .progressPercent(80)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 6, 30))
            .build(),
        Goal.builder()
            .appraisal(sales2H1)
            .employee(sales2)
            .title("Book 40 qualified demos")
            .description("Improve lead follow-up and outreach personalization.")
            .progressPercent(60)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 5, 31))
            .build(),
        Goal.builder()
            .appraisal(engManagerH1)
            .employee(engManager)
            .title("Increase team delivery predictability")
            .description("Improve sprint planning and reduce carryover by 20%.")
            .progressPercent(45)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 6, 20))
            .build(),
        Goal.builder()
            .appraisal(salesManagerH1)
            .employee(salesManager)
            .title("Launch manager enablement playbook")
            .description("Standardize coaching and enablement for new reps.")
            .progressPercent(65)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 5, 25))
            .build(),
        Goal.builder()
            .appraisal(financeManagerH1)
            .employee(financeManager)
            .title("Automate finance dashboard updates")
            .description("Reduce manual reporting effort by 30%.")
            .progressPercent(30)
            .status(Goal.Status.IN_PROGESS)
            .dueDate(LocalDate.of(2026, 6, 5))
            .build(),
        Goal.builder()
            .appraisal(finance1H1)
            .employee(finance1)
            .title("Automate monthly close checklist")
            .description("Create playbooks and automate variance checks.")
            .progressPercent(100)
            .status(Goal.Status.COMPLETED)
            .dueDate(LocalDate.of(2026, 3, 31))
            .build()));

    notificationRepository.saveAll(List.of(
        Notification.builder()
            .user(eng1)
            .title("Self-assessment submitted")
            .message("Your self-assessment for 2026 H1 is submitted. Waiting on manager review.")
            .type(Notification.Type.SELF_ASSESSMENT_SUBMITTED)
            .isRead(false)
            .build(),
        Notification.builder()
            .user(engManager)
            .title("Pending reviews")
            .message("You have 2 appraisals awaiting review this cycle.")
            .type(Notification.Type.APPRAISAL_DUE)
            .isRead(false)
            .build(),
        Notification.builder()
            .user(sales1)
            .title("Manager review completed")
            .message("Your manager review is complete for 2026 H1.")
            .type(Notification.Type.MANAGER_REVIEW_DONE)
            .isRead(true)
            .build(),
        Notification.builder()
            .user(opsVp)
            .title("Manager self-assessments submitted")
            .message("3 manager self-assessments are ready for review this cycle.")
            .type(Notification.Type.APPRAISAL_DUE)
            .isRead(false)
            .build(),
        Notification.builder()
            .user(hrLead)
            .title("Cycle report ready")
            .message("2026 H1 cycle report is ready for leadership review.")
            .type(Notification.Type.GENERAL)
            .isRead(false)
            .build(),
        Notification.builder()
            .user(finance1)
            .title("Appraisal approved")
            .message("Your 2026 H1 appraisal has been approved.")
            .type(Notification.Type.APPRAISAL_APPROVED)
            .isRead(true)
            .build()));
  }
}
