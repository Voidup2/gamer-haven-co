package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.domain.MarketplaceTransaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MarketplaceTransactionResponse(
        UUID id,
        UUID listingId,
        String listingTitle,
        String gameId,
        Long buyerId,
        String buyerUsername,
        Long sellerId,
        String sellerUsername,
        BigDecimal amount,
        MarketplaceTransaction.Status status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static MarketplaceTransactionResponse from(MarketplaceTransaction t) {
        return new MarketplaceTransactionResponse(
                t.getId(), t.getListing().getId(), t.getListing().getTitle(),
                t.getListing().getGame().getId(), t.getBuyer().getId(), t.getBuyer().getUsername(),
                t.getSeller().getId(), t.getSeller().getUsername(), t.getAmount(), t.getStatus(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}