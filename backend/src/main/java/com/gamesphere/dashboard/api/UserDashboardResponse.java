package com.gamesphere.dashboard.api;

import com.gamesphere.activity.api.UserActivitySummaryResponse;
import com.gamesphere.statistics.api.UserStatisticsResponse;

public record UserDashboardResponse(
        UserStatisticsResponse statistics,
        UserActivitySummaryResponse activity,
        long unreadNotifications
) {}
