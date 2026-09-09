package com.gamesphere.chat;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatRealtimeEvent(
        ChatRealtimeEventType type,
        UUID roomId,
        UUID messageId,
        Long userId,
        String username,
        OffsetDateTime occurredAt
) {}
