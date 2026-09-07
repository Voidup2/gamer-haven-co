package com.gamesphere.achievements.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AchievementRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description,
        @Min(0) int points
) {}