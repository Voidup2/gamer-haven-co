package com.gamesphere.statistics.api;

public record GameStatisticsResponse(
        String gameId,
        String title,
        long playersTracked,
        long completedPlayers,
        int completionRatePercent,
        long totalPlaytimeMinutes,
        long libraryOwners,
        long reviewCount,
        double averageReviewRating,
        long totalAchievements,
        long achievementUnlocks,
        int achievementCompletionPercent
) {}