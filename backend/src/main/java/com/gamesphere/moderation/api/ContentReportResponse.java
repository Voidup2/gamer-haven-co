package com.gamesphere.moderation.api;

import com.gamesphere.moderation.domain.ContentReport;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContentReportResponse(
        UUID id,
        UUID discussionId,
        UUID commentId,
        Long reporterUserId,
        String reason,
        ContentReport.Status status,
        OffsetDateTime createdAt,
        OffsetDateTime reviewedAt
) {
    public static ContentReportResponse from(ContentReport report) {
        return new ContentReportResponse(
                report.getId(),
                report.getDiscussion() == null ? null : report.getDiscussion().getId(),
                report.getComment() == null ? null : report.getComment().getId(),
                report.getReporter().getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }
}
