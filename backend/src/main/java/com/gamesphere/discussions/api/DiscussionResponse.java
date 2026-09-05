package com.gamesphere.discussions.api;

import com.gamesphere.discussions.domain.Discussion;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DiscussionResponse(
        UUID id,
        String gameId,
        Long userId,
        String username,
        String displayName,
        String title,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DiscussionResponse from(Discussion discussion) {
        return new DiscussionResponse(
                discussion.getId(),
                discussion.getGame().getId(),
                discussion.getUser().getId(),
                discussion.getUser().getUsername(),
                discussion.getUser().getDisplayName(),
                discussion.getTitle(),
                discussion.getContent(),
                discussion.getCreatedAt(),
                discussion.getUpdatedAt()
        );
    }
}