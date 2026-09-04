package com.gamesphere.library.repository;

import com.gamesphere.library.domain.UserGameWishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGameWishlistRepository
        extends JpaRepository<UserGameWishlist, Long> {

    boolean existsByUserIdAndGameId(Long userId, String gameId);

    List<UserGameWishlist> findByUserId(Long userId);

    void deleteByUserIdAndGameId(Long userId, String gameId);
}