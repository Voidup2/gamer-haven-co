package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.domain.GameListing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GameListingResponse(
        UUID id,
        String gameId,
        String gameTitle,
        Long sellerId,
        String sellerUsername,
        String sellerDisplayName,
        double sellerRating,
        long sellerReviewCount,
        String title,
        String imageUrl,
        String description,
        GameListing.Condition condition,
        BigDecimal price,
        String platform,
        String location,
        String contactEmail,
        String contactPhone,
        boolean boxIncluded,
        boolean manualIncluded,
        GameListing.Status status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static GameListingResponse from(GameListing listing, double sellerRating, long sellerReviewCount) {
        return new GameListingResponse(
                listing.getId(), listing.getGame().getId(), listing.getGame().getTitle(),
                listing.getSeller().getId(), listing.getSeller().getUsername(), listing.getSeller().getDisplayName(),
                sellerRating, sellerReviewCount,
                listing.getTitle(), listing.getImageUrl(), listing.getDescription(), listing.getCondition(),
                listing.getPrice(), listing.getPlatform(), listing.getLocation(), listing.getContactEmail(),
                listing.getContactPhone(), listing.isBoxIncluded(), listing.isManualIncluded(), listing.getStatus(),
                listing.getCreatedAt(), listing.getUpdatedAt()
        );
    }

    public static GameListingResponse from(GameListing listing) {
        return from(listing, 0.0, 0L);
    }
}
