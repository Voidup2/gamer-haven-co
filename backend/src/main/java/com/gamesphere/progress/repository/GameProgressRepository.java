package com.gamesphere.progress.repository;

import com.gamesphere.progress.domain.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameProgressRepository extends JpaRepository<GameProgress, UUID> {
    List<GameProgress> findByUserIdOrderByLastPlayedAtDesc(Long userId);
    List<GameProgress> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, GameProgress.Status status);
    Optional<GameProgress> findByUserIdAndGameId(Long userId, String gameId);
    boolean existsByUserIdAndGameId(Long userId, String gameId);
}