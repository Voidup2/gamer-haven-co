package com.gamesphere.progress.api;

import com.gamesphere.progress.domain.GameProgress;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GameProgressResponse(
        UUID id,
        String gameId,
        String gameTitle,
        String coverUrl,
        GameProgress.Status status,
        int playtimeMinutes,
        int progressPercent,
        String notes,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime lastPlayedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static GameProgressResponse from(GameProgress progress) {
        return new GameProgressResponse(progress.getId(), progress.getGame().getId(), progress.getGame().getTitle(),
                progress.getGame().getCoverUrl(), progress.getStatus(), progress.getPlaytimeMinutes(),
                progress.getProgressPercent(), progress.getNotes(), progress.getStartedAt(), progress.getCompletedAt(),
                progress.getLastPlayedAt(), progress.getCreatedAt(), progress.getUpdatedAt());
    }
}