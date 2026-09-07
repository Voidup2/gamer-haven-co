package com.gamesphere.chat;

public record ChatPresenceEvent(
        Long userId,
        String username,
        String displayName,
        ChatPresenceStatus status
) {}
