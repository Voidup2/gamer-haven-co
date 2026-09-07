package com.gamesphere.mygames.api;

import com.gamesphere.progress.domain.GameProgress;

import java.time.OffsetDateTime;
import java.util.Map;

public record MyGamesSummaryResponse(
        long libraryCount,
        long wishlistCount,
        long wishlistLibraryOverlapCount,
        long trackedGameCount,
        long completedCount,
        long playingCount,
        long onHoldCount,
        long droppedCount,
        long notStartedCount,
        long totalPlaytimeMinutes,
        long collectionCount,
        long collectionGameCount,
        Map<GameProgress.Status, Long> progressByStatus,
        OffsetDateTime latestPlayedAt
) {}
