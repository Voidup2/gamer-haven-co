package com.gamesphere.achievements.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AchievementProgressRequest(@Min(0) @Max(100) int progressPercent) {}