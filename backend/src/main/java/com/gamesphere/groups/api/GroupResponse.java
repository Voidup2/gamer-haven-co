package com.gamesphere.groups.api;

import com.gamesphere.groups.domain.GameGroup;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        Long ownerId,
        String ownerUsername,
        String name,
        String description,
        String imageUrl,
        boolean publicGroup,
        long memberCount,
        boolean member,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static GroupResponse from(GameGroup group, long memberCount, boolean member) {
        return new GroupResponse(
                group.getId(),
                group.getOwner().getId(),
                group.getOwner().getUsername(),
                group.getName(),
                group.getDescription(),
                group.getImageUrl(),
                group.isPublicGroup(),
                memberCount,
                member,
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
