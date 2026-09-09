package com.gamesphere.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ForumDtos {
    private ForumDtos() {}

    public record CreateTopicRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 40) String category,
            @NotBlank @Size(max = 5000) String content,
            @Size(max = 100) String gameId) {}

    public record UpdateTopicRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 40) String category,
            @NotBlank @Size(max = 5000) String content,
            @Size(max = 100) String gameId) {}

    public record CreatePostRequest(@NotBlank @Size(max = 5000) String content) {}
    public record UpdatePostRequest(@NotBlank @Size(max = 5000) String content) {}

    public record TopicResponse(
            UUID id, String title, String category, String content,
            Long authorId, String authorUsername, String authorDisplayName,
            String gameId, boolean pinned, boolean locked, long viewCount,
            long postCount, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}

    public record PostResponse(
            UUID id, UUID topicId, Long authorId, String authorUsername,
            String authorDisplayName, String content,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
}
