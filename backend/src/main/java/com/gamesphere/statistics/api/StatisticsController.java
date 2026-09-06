package com.gamesphere.statistics.api;

import com.gamesphere.statistics.service.GameStatisticsService;
import com.gamesphere.statistics.service.UserStatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class StatisticsController {
    private final GameStatisticsService gameStatisticsService;
    private final UserStatisticsService userStatisticsService;

    public StatisticsController(GameStatisticsService gameStatisticsService, UserStatisticsService userStatisticsService) {
        this.gameStatisticsService = gameStatisticsService; this.userStatisticsService = userStatisticsService;
    }

    @GetMapping("/games/{gameId}/statistics")
    public GameStatisticsResponse gameStatistics(@PathVariable String gameId) { return gameStatisticsService.game(gameId); }

    @GetMapping("/users/me/statistics")
    @PreAuthorize("isAuthenticated()")
    public UserStatisticsResponse userStatistics() { return userStatisticsService.mine(); }
}