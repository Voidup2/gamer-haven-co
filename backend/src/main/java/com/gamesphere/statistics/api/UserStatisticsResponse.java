package com.gamesphere.statistics.api;

public record UserStatisticsResponse(
        long trackedGames,
        long completedGames,
        int completionRatePercent,
        long totalPlaytimeMinutes,
        double averageProgressPercent,
        long reviewCount,
        long achievementsUnlocked,
        long achievementPoints
) {}