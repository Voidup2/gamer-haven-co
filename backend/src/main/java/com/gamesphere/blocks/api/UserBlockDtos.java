package com.gamesphere.blocks.api;

import java.time.OffsetDateTime;

public final class UserBlockDtos {
    private UserBlockDtos() {}
    public record BlockResponse(Long userId, String username, String displayName, OffsetDateTime blockedAt) {}
}
