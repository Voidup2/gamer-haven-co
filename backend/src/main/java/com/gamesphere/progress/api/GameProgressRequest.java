package com.gamesphere.progress.api;

import com.gamesphere.progress.domain.GameProgress.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GameProgressRequest(
        @NotNull Status status,
        @Min(0) int playtimeMinutes,
        @Min(0) @Max(100) int progressPercent,
        @Size(max = 5000) String notes
) {}