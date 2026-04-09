package dev.nishants.appraisal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.nishants.appraisal.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n
            from Notification n
            join fetch n.user
            where n.id = :id
            """)
    Optional<Notification> findByIdWithUser(@Param("id") Long id);

    @Query("""
            select n
            from Notification n
            join fetch n.user
            where n.user.id = :userId
            order by n.createdAt desc
            """)
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("""
            select n
            from Notification n
            join fetch n.user
            where n.user.id = :userId
              and n.isRead = false
            """)
    List<Notification> findByUserIdAndIsReadFalse(@Param("userId") Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}
