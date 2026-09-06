package com.gamesphere.achievements.api;

public record AchievementStatsResponse(String gameId, long totalAchievements, long unlockedAchievements, int completionPercent, int earnedPoints) {}