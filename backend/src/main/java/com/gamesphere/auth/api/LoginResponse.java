package com.gamesphere.auth.api;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String username
) {}
