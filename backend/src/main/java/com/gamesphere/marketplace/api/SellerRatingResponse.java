package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.domain.SellerRating;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SellerRatingResponse(
        UUID id,
        UUID listingId,
        Long sellerId,
        Long reviewerId,
        String reviewerUsername,
        String reviewerDisplayName,
        short rating,
        String review,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static SellerRatingResponse from(SellerRating rating) {
        return new SellerRatingResponse(
                rating.getId(), rating.getListing().getId(), rating.getSeller().getId(),
                rating.getReviewer().getId(), rating.getReviewer().getUsername(),
                rating.getReviewer().getDisplayName(), rating.getRating(), rating.getReview(),
                rating.getCreatedAt(), rating.getUpdatedAt()
        );
    }
}
