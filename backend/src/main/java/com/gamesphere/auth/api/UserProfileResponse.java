package com.gamesphere.auth.api;

import com.gamesphere.auth.domain.User;

import java.time.OffsetDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String displayName,
        boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
