package com.gamesphere.moderation.repository;

import com.gamesphere.moderation.domain.ContentReport;
import com.gamesphere.moderation.domain.ContentReport.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentReportRepository extends JpaRepository<ContentReport, UUID> {
    boolean existsByReporterIdAndDiscussionId(Long reporterId, UUID discussionId);
    boolean existsByReporterIdAndCommentId(Long reporterId, UUID commentId);
    List<ContentReport> findByStatusOrderByCreatedAtAsc(Status status);
}
