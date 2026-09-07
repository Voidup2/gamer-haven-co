package com.gamesphere.chat;

import java.util.UUID;

public record ChatTypingEvent(
        UUID roomId,
        Long userId,
        String username,
        boolean typing
) {}
