package com.gamesphere.activity.api;

import com.gamesphere.activity.domain.UserActivity;

import java.time.OffsetDateTime;
import java.util.Map;

public record UserActivitySummaryResponse(
        long totalActivities,
        Map<UserActivity.ActivityType, Long> countsByType,
        OffsetDateTime latestActivityAt,
        long progressUpdates,
        long gamesCompleted,
        long achievementsUnlocked,
        long marketplacePurchases,
        long marketplaceSales
) {}
