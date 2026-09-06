package com.gamesphere.collections.api;

import com.gamesphere.collections.domain.GameCollectionItem;
import java.time.OffsetDateTime;

public record GameCollectionItemResponse(
        String gameId,
        String title,
        String coverUrl,
        OffsetDateTime addedAt
) {
    public static GameCollectionItemResponse from(GameCollectionItem item) {
        return new GameCollectionItemResponse(item.getGame().getId(), item.getGame().getTitle(),
                item.getGame().getCoverUrl(), item.getAddedAt());
    }
}