package com.gamesphere.activity.repository;

import com.gamesphere.activity.domain.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {
    Page<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(Long userId, UserActivity.ActivityType activityType, Pageable pageable);
    Page<UserActivity> findByUserIdAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
            Long userId, String referenceType, String referenceId, Pageable pageable);
    Optional<UserActivity> findFirstByUserIdAndActivityTypeAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
            Long userId, UserActivity.ActivityType activityType, String referenceType, String referenceId);
    long countByUserId(Long userId);
    long countByUserIdAndActivityType(Long userId, UserActivity.ActivityType activityType);
    Optional<UserActivity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    @Transactional
    void deleteByUserId(Long userId);

    @Query("select a.createdAt from UserActivity a where a.user.id = :userId order by a.createdAt desc")
    List<OffsetDateTime> findCreatedAtByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("""
            select a.referenceId, count(a)
            from UserActivity a
            where a.user.id = :userId
              and a.referenceType = 'GAME'
              and a.referenceId is not null
            group by a.referenceId
            order by count(a) desc
            """)
    List<Object[]> findGameActivityCounts(@Param("userId") Long userId, Pageable pageable);
}
