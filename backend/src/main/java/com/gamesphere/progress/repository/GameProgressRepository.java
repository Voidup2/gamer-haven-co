package com.gamesphere.progress.repository;

import com.gamesphere.progress.domain.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameProgressRepository extends JpaRepository<GameProgress, UUID> {
    List<GameProgress> findByUserIdOrderByLastPlayedAtDesc(Long userId);
    List<GameProgress> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, GameProgress.Status status);
    Optional<GameProgress> findByUserIdAndGameId(Long userId, String gameId);
    boolean existsByUserIdAndGameId(Long userId, String gameId);

    long countByGameId(String gameId);
    long countByGameIdAndStatus(String gameId, GameProgress.Status status);
    long countByUserId(Long userId);

    @Query("select coalesce(sum(p.playtimeMinutes), 0) from GameProgress p where p.user.id = :userId")
    long sumPlaytimeByUserId(Long userId);

    @Query("select coalesce(sum(p.playtimeMinutes), 0) from GameProgress p where p.user.id = :userId and p.game.id = :gameId")
    long sumPlaytimeByUserIdAndGameId(Long userId, String gameId);

    @Query("select coalesce(avg(p.progressPercent), 0) from GameProgress p where p.user.id = :userId")
    double averageProgressByUserId(Long userId);
}