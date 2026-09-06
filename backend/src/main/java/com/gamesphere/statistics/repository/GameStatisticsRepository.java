package com.gamesphere.statistics.repository;

import com.gamesphere.games.domain.Game;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface GameStatisticsRepository extends Repository<Game, String> {
    @Query("select count(l) from UserGameLibrary l where l.game.id = :gameId")
    long libraryCount(@Param("gameId") String gameId);
    @Query("select count(r) from Review r where r.game.id = :gameId")
    long reviewCount(@Param("gameId") String gameId);
    @Query("select coalesce(avg(r.rating), 0) from Review r where r.game.id = :gameId")
    double averageReviewRating(@Param("gameId") String gameId);
    @Query("select count(p) from GameProgress p where p.game.id = :gameId")
    long playerCount(@Param("gameId") String gameId);
    @Query("select count(p) from GameProgress p where p.game.id = :gameId and p.status = com.gamesphere.progress.domain.GameProgress$Status.COMPLETED")
    long completedCount(@Param("gameId") String gameId);
    @Query("select coalesce(sum(p.playtimeMinutes), 0) from GameProgress p where p.game.id = :gameId")
    long totalPlaytimeMinutes(@Param("gameId") String gameId);
    @Query("select count(a) from GameAchievement a where a.game.id = :gameId")
    long achievementCount(@Param("gameId") String gameId);
    @Query("select count(u) from UserGameAchievement u where u.achievement.game.id = :gameId and u.unlockedAt is not null")
    long achievementUnlockCount(@Param("gameId") String gameId);
    @Query("select count(p) from GameProgress p where p.user.id = :userId")
    long userTrackedGames(@Param("userId") Long userId);
    @Query("select count(p) from GameProgress p where p.user.id = :userId and p.status = com.gamesphere.progress.domain.GameProgress$Status.COMPLETED")
    long userCompletedGames(@Param("userId") Long userId);
    @Query("select coalesce(sum(p.playtimeMinutes), 0) from GameProgress p where p.user.id = :userId")
    long userPlaytimeMinutes(@Param("userId") Long userId);
    @Query("select coalesce(avg(p.progressPercent), 0) from GameProgress p where p.user.id = :userId")
    double userAverageProgress(@Param("userId") Long userId);
    @Query("select count(r) from Review r where r.user.id = :userId")
    long userReviewCount(@Param("userId") Long userId);
    @Query("select count(u) from UserGameAchievement u where u.user.id = :userId and u.unlockedAt is not null")
    long userAchievementCount(@Param("userId") Long userId);
    @Query("select coalesce(sum(u.achievement.points), 0) from UserGameAchievement u where u.user.id = :userId and u.unlockedAt is not null")
    long userAchievementPoints(@Param("userId") Long userId);
}