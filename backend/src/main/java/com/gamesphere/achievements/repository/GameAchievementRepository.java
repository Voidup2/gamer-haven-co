package com.gamesphere.achievements.repository;

import com.gamesphere.achievements.domain.GameAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GameAchievementRepository extends JpaRepository<GameAchievement, UUID> {
    List<GameAchievement> findByGameIdOrderByPointsAscNameAsc(String gameId);
    boolean existsByGameIdAndName(String gameId, String name);
    boolean existsByGameIdAndNameAndIdNot(String gameId, String name, UUID id);
}