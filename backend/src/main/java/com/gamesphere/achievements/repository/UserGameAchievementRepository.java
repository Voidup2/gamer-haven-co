package com.gamesphere.achievements.repository;

import com.gamesphere.achievements.domain.UserGameAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGameAchievementRepository extends JpaRepository<UserGameAchievement, UUID> {
    List<UserGameAchievement> findByUserIdOrderByUnlockedAtDesc(Long userId);
    List<UserGameAchievement> findByAchievementGameIdAndUserId(String gameId, Long userId);
    Optional<UserGameAchievement> findByAchievementIdAndUserId(UUID achievementId, Long userId);
    long countByAchievementGameId(String gameId);
    long countByAchievementGameIdAndUnlockedAtIsNotNull(String gameId);
    long countByAchievementGameIdAndUserIdAndUnlockedAtIsNotNull(String gameId, Long userId);
}