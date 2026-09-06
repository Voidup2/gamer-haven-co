package com.gamesphere.statistics.service;

import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.statistics.api.UserStatisticsResponse;
import com.gamesphere.statistics.repository.GameStatisticsRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserStatisticsService {
    private final GameStatisticsRepository statistics;
    private final UserRepository userRepository;

    public UserStatisticsService(GameStatisticsRepository statistics, UserRepository userRepository) {
        this.statistics = statistics; this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserStatisticsResponse mine() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) throw new AccessDeniedException("Authentication required");
        var user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        long tracked = statistics.userTrackedGames(user.getId());
        long completed = statistics.userCompletedGames(user.getId());
        return new UserStatisticsResponse(tracked, completed, percent(completed, tracked),
                statistics.userPlaytimeMinutes(user.getId()), statistics.userAverageProgress(user.getId()),
                statistics.userReviewCount(user.getId()), statistics.userAchievementCount(user.getId()),
                statistics.userAchievementPoints(user.getId()));
    }

    private int percent(long value, long total) { return total == 0 ? 0 : (int) Math.round(value * 100.0 / total); }
}