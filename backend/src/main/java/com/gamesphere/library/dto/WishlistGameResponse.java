package com.gamesphere.library.dto;

import java.time.OffsetDateTime;

public class WishlistGameResponse {

    private final String gameId;
    private final String title;
    private final OffsetDateTime addedAt;

    public WishlistGameResponse(
            String gameId,
            String title,
            OffsetDateTime addedAt
    ) {
        this.gameId = gameId;
        this.title = title;
        this.addedAt = addedAt;
    }

    public String getGameId() {
        return gameId;
    }

    public String getTitle() {
        return title;
    }

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }
}