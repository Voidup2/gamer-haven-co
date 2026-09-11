package com.gamesphere.auth.api;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        Long userId,
        String username
) {}
