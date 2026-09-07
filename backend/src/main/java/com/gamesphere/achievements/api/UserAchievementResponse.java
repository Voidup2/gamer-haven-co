package com.gamesphere.achievements.api;

import com.gamesphere.achievements.domain.UserGameAchievement;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserAchievementResponse(UUID id, UUID achievementId, String name, String description, int points, int progressPercent, OffsetDateTime unlockedAt, OffsetDateTime updatedAt) {
    public static UserAchievementResponse from(UserGameAchievement u) {
        var a = u.getAchievement();
        return new UserAchievementResponse(u.getId(), a.getId(), a.getName(), a.getDescription(), a.getPoints(), u.getProgressPercent(), u.getUnlockedAt(), u.getUpdatedAt());
    }
}