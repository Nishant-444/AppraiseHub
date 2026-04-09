package dev.nishants.appraisal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.nishants.appraisal.entity.User;
import dev.nishants.appraisal.entity.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
            select u
            from User u
            left join fetch u.manager
            left join fetch u.department
            where u.email = :email
            """)
    Optional<User> findByEmailWithDetails(@Param("email") String email);

    boolean existsByEmail(String email);

    @Query("""
            select distinct u
            from User u
            left join fetch u.manager
            left join fetch u.department
            """)
    List<User> findAllWithDetails();

    @Query("""
            select u
            from User u
            left join fetch u.manager
            left join fetch u.department
            where u.id = :id
            """)
    Optional<User> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            select distinct u
            from User u
            left join fetch u.manager
            left join fetch u.department
            where u.manager.id = :managerId
            """)
    List<User> findByManagerId(@Param("managerId") Long managerId);

    @Query("""
            select distinct u
            from User u
            left join fetch u.manager
            left join fetch u.department
            where u.department.id = :departmentId
            """)
    List<User> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("""
            select distinct u
            from User u
            left join fetch u.manager
            left join fetch u.department
            where u.isActive = true
            """)
    List<User> findByIsActiveTrue();

    @Query("""
            select distinct u
            from User u
            left join fetch u.manager
            left join fetch u.department
            where u.role = :role and u.isActive = true
            """)
    List<User> findByRoleAndIsActiveTrue(@Param("role") Role role);

    List<User> findByDepartmentIdAndIsActiveTrue(Long departmentId);
}
