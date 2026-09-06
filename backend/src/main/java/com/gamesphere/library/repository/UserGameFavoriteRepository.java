package com.gamesphere.library.repository;

import com.gamesphere.library.domain.UserGameFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGameFavoriteRepository extends JpaRepository<UserGameFavorite, Long> {

    boolean existsByUserIdAndGameId(Long userId, String gameId);

    List<UserGameFavorite> findByUserIdOrderByAddedAtDesc(Long userId);

    void deleteByUserIdAndGameId(Long userId, String gameId);
}
