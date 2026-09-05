package com.gamesphere.moderation.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.comments.domain.Comment;
import com.gamesphere.comments.repository.CommentRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.discussions.domain.Discussion;
import com.gamesphere.discussions.repository.DiscussionRepository;
import com.gamesphere.moderation.api.ContentReportRequest;
import com.gamesphere.moderation.api.ContentReportResponse;
import com.gamesphere.moderation.domain.ContentReport;
import com.gamesphere.moderation.domain.ContentReport.Status;
import com.gamesphere.moderation.repository.ContentReportRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ContentReportService {
    private final ContentReportRepository reportRepository;
    private final DiscussionRepository discussionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public ContentReportService(ContentReportRepository reportRepository,
                                DiscussionRepository discussionRepository,
                                CommentRepository commentRepository,
                                UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.discussionRepository = discussionRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ContentReportResponse reportDiscussion(UUID discussionId, ContentReportRequest request) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));
        User user = getCurrentUser();
        if (reportRepository.existsByReporterIdAndDiscussionId(user.getId(), discussionId)) {
            throw new ConflictException("You have already reported this discussion");
        }
        return save(new ContentReport(user, discussion, null, request.reason()));
    }

    @Transactional
    public ContentReportResponse reportComment(UUID commentId, ContentReportRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        User user = getCurrentUser();
        if (reportRepository.existsByReporterIdAndCommentId(user.getId(), commentId)) {
            throw new ConflictException("You have already reported this comment");
        }
        return save(new ContentReport(user, null, comment, request.reason()));
    }

    @Transactional(readOnly = true)
    public List<ContentReportResponse> findByStatus(Status status) {
        requireAdmin();
        return reportRepository.findByStatusOrderByCreatedAtAsc(status)
                .stream().map(ContentReportResponse::from).toList();
    }

    @Transactional
    public ContentReportResponse review(UUID id, Status status) {
        requireAdmin();
        ContentReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        if (status == Status.PENDING) {
            throw new IllegalArgumentException("Report cannot be reviewed with PENDING status");
        }
        report.review(status);
        return ContentReportResponse.from(reportRepository.save(report));
    }

    private ContentReportResponse save(ContentReport report) {
        return ContentReportResponse.from(reportRepository.save(report));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void requireAdmin() {
        User user = getCurrentUser();
        boolean admin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        if (!admin) {
            throw new AccessDeniedException("Administrator access required");
        }
    }
}
