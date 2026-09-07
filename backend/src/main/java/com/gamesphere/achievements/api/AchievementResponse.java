package com.gamesphere.achievements.api;

import com.gamesphere.achievements.domain.GameAchievement;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AchievementResponse(UUID id, String gameId, String name, String description, int points, OffsetDateTime createdAt) {
    public static AchievementResponse from(GameAchievement a) {
        return new AchievementResponse(a.getId(), a.getGame().getId(), a.getName(), a.getDescription(), a.getPoints(), a.getCreatedAt());
    }
}