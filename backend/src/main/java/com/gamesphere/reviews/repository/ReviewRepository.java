package com.gamesphere.reviews.repository;

import com.gamesphere.reviews.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByGameIdOrderByCreatedAtDesc(String gameId);

    Optional<Review> findByUserIdAndGameId(
            Long userId,
            String gameId
    );

    boolean existsByUserIdAndGameId(
            Long userId,
            String gameId
    );

    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.game.id = :gameId
            """)
    BigDecimal calculateAverageRating(String gameId);

    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.game.id = :gameId
            """)
    long countByGameIdForRating(String gameId);
}