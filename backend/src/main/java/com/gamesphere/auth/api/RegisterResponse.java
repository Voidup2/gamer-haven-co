package com.gamesphere.auth.api;

public record RegisterResponse(
        Long id,
        String username,
        String email,
        String displayName
) {
}
