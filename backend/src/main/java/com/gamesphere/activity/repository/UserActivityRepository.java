package com.gamesphere.activity.repository;

import com.gamesphere.activity.domain.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
