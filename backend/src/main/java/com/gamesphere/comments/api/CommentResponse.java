package com.gamesphere.comments.api;

import com.gamesphere.comments.domain.Comment;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID discussionId,
        Long userId,
        String username,
        String displayName,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getDiscussion().getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getDisplayName(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}