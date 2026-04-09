package dev.nishants.appraisal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.nishants.appraisal.entity.Goal;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    @Query("""
            select g
            from Goal g
            join fetch g.employee
            join fetch g.appraisal
            where g.id = :id
            """)
    Optional<Goal> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            select g
            from Goal g
            join fetch g.employee
            join fetch g.appraisal
            where g.appraisal.id = :appraisalId
            """)
    List<Goal> findByAppraisalId(@Param("appraisalId") Long appraisalId);

    @Query("""
            select g
            from Goal g
            join fetch g.employee
            join fetch g.appraisal
            where g.employee.id = :employeeId
            """)
    List<Goal> findByEmployeeId(@Param("employeeId") Long employeeId);

    // ── Report queries ────────────────────────────────────────────
    long countByAppraisalId(Long appraisalId);

    long countByAppraisalIdAndStatus(Long appraisalId, Goal.Status status);
}
