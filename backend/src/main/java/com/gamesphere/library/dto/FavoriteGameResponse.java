package com.gamesphere.library.dto;

import java.time.OffsetDateTime;

public record FavoriteGameResponse(
        String gameId,
        String title,
        String coverUrl,
        OffsetDateTime addedAt
) {
}
