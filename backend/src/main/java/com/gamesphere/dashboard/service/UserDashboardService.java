package com.gamesphere.dashboard.service;

import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.dashboard.api.UserDashboardResponse;
import com.gamesphere.notifications.service.NotificationService;
import com.gamesphere.statistics.service.UserStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDashboardService {
    private final UserStatisticsService statisticsService;
    private final UserActivityService activityService;
    private final NotificationService notificationService;

    public UserDashboardService(UserStatisticsService statisticsService,
                                UserActivityService activityService,
                                NotificationService notificationService) {
        this.statisticsService = statisticsService;
        this.activityService = activityService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse mine() {
        return new UserDashboardResponse(
                statisticsService.mine(),
                activityService.summary(),
                notificationService.unreadCount()
        );
    }
}
