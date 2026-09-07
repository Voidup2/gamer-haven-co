package com.gamesphere.statistics.service;

import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.statistics.api.*;
import com.gamesphere.statistics.repository.GameStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameStatisticsService {
    private final GameRepository gameRepository;
    private final GameStatisticsRepository statistics;

    public GameStatisticsService(GameRepository gameRepository, GameStatisticsRepository statistics) {
        this.gameRepository = gameRepository; this.statistics = statistics;
    }

    @Transactional(readOnly = true)
    public GameStatisticsResponse game(String gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        long players = statistics.playerCount(gameId);
        long completed = statistics.completedCount(gameId);
        long achievements = statistics.achievementCount(gameId);
        long unlocks = statistics.achievementUnlockCount(gameId);
        return new GameStatisticsResponse(gameId, game.getTitle(), players, completed,
                percent(completed, players), statistics.totalPlaytimeMinutes(gameId), statistics.libraryCount(gameId),
                statistics.reviewCount(gameId), statistics.averageReviewRating(gameId), achievements, unlocks,
                percent(unlocks, achievements));
    }

    private int percent(long value, long total) { return total == 0 ? 0 : (int) Math.round(value * 100.0 / total); }
}