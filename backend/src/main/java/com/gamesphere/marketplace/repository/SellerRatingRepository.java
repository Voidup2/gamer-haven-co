package com.gamesphere.marketplace.repository;

import com.gamesphere.marketplace.domain.SellerRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SellerRatingRepository extends JpaRepository<SellerRating, UUID> {
    Optional<SellerRating> findByListingIdAndReviewerId(UUID listingId, Long reviewerId);
    Page<SellerRating> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    long countBySellerId(Long sellerId);
    Double findAverageRatingBySellerId(Long sellerId);
}
