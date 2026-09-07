package com.gamesphere.collections.api;

import com.gamesphere.collections.domain.GameCollection;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GameCollectionResponse(
        UUID id,
        Long userId,
        String username,
        String name,
        String description,
        boolean publicCollection,
        long gameCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static GameCollectionResponse from(GameCollection collection, long gameCount) {
        return new GameCollectionResponse(collection.getId(), collection.getUser().getId(),
                collection.getUser().getUsername(), collection.getName(), collection.getDescription(),
                collection.isPublicCollection(), gameCount, collection.getCreatedAt(), collection.getUpdatedAt());
    }
}