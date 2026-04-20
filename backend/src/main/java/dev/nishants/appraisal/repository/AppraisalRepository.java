package dev.nishants.appraisal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.nishants.appraisal.entity.Appraisal;
import dev.nishants.appraisal.entity.enums.AppraisalStatus;

public interface AppraisalRepository extends JpaRepository<Appraisal, Long> {

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee e
      left join fetch e.department
      join fetch a.manager
      """)
  List<Appraisal> findAllWithDetails();

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee e
      left join fetch e.department
      join fetch a.manager
      where a.id = :id
      """)
  Optional<Appraisal> findByIdWithDetails(@Param("id") Long id);

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee e
      left join fetch e.department
      join fetch a.manager
      where a.employee.id = :employeeId
      """)
  List<Appraisal> findByEmployeeId(@Param("employeeId") Long employeeId);

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee e
      left join fetch e.department
      join fetch a.manager
      where a.manager.id = :managerId
      """)
  List<Appraisal> findByManagerId(@Param("managerId") Long managerId);

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee
      join fetch a.manager
      where a.cycleName = :cycleName
      """)
  List<Appraisal> findByCycleName(@Param("cycleName") String cycleName);

  boolean existsByCycleNameAndEmployeeId(String cycleName, Long employeeId);

  boolean existsByEmployeeIdAndCycleStartDateBetween(Long employeeId,
      LocalDate startDate,
      LocalDate endDate);

  @Query("""
      select distinct a.cycleName
      from Appraisal a
      where a.cycleStartDate between :startDate and :endDate
      """)
  List<String> findDistinctCycleNamesInYear(@Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  Optional<Appraisal> findFirstByCycleNameAndCycleStartDateBetween(String cycleName,
      LocalDate startDate,
      LocalDate endDate);

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee
      join fetch a.manager
      where a.cycleName = :cycleName
        and a.employee.id = :employeeId
      """)
  Optional<Appraisal> findByCycleNameAndEmployeeId(@Param("cycleName") String cycleName,
      @Param("employeeId") Long employeeId);

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee
      join fetch a.manager
      where a.cycleName = :cycleName
        and a.appraisalStatus = :status
      """)
  List<Appraisal> findByCycleNameAndAppraisalStatus(@Param("cycleName") String cycleName,
      @Param("status") AppraisalStatus status);

  @Query("""
      select a
      from Appraisal a
      join fetch a.employee
      join fetch a.manager
      where a.cycleName = :cycleName
        and a.manager.id = :managerId
      """)
  List<Appraisal> findByCycleNameAndManagerId(@Param("cycleName") String cycleName,
      @Param("managerId") Long managerId);

  // ── Report queries ────────────────────────────────────────────

  @Query("SELECT a.appraisalStatus, COUNT(a) FROM Appraisal a WHERE a.cycleName = :cycleName GROUP BY a.appraisalStatus")
  List<Object[]> countByStatusForCycle(@Param("cycleName") String cycleName);

  @Query("SELECT AVG(a.managerRating) FROM Appraisal a WHERE a.cycleName = :cycleName AND a.appraisalStatus IN ('APPROVED','ACKNOWLEDGED') AND a.managerRating IS NOT NULL")
  Double averageManagerRatingForCycle(@Param("cycleName") String cycleName);

  @Query("SELECT a.managerRating, COUNT(a) FROM Appraisal a WHERE a.cycleName = :cycleName AND a.appraisalStatus IN ('APPROVED','ACKNOWLEDGED') AND a.managerRating IS NOT NULL GROUP BY a.managerRating ORDER BY a.managerRating")
  List<Object[]> getRatingDistribution(@Param("cycleName") String cycleName);

  @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee e LEFT JOIN FETCH e.department WHERE a.cycleName = :cycleName AND a.manager.id = :managerId")
  List<Appraisal> findTeamAppraisalsForCycle(@Param("cycleName") String cycleName, @Param("managerId") Long managerId);

  @Query("SELECT AVG(a.managerRating) FROM Appraisal a WHERE a.cycleName = :cycleName AND a.manager.id = :managerId AND a.managerRating IS NOT NULL")
  Double averageRatingForTeam(@Param("cycleName") String cycleName, @Param("managerId") Long managerId);

  @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee e LEFT JOIN FETCH e.department JOIN FETCH a.manager WHERE a.cycleName = :cycleName AND a.appraisalStatus NOT IN ('APPROVED','ACKNOWLEDGED')")
  List<Appraisal> findPendingAppraisalsForCycle(@Param("cycleName") String cycleName);

  @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee JOIN FETCH a.manager WHERE a.employee.id = :employeeId ORDER BY a.cycleStartDate DESC")
  List<Appraisal> findEmployeeHistory(@Param("employeeId") Long employeeId);
}
