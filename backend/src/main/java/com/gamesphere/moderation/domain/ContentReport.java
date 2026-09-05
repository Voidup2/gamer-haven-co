package com.gamesphere.moderation.domain;

import com.gamesphere.auth.domain.User;
import com.gamesphere.comments.domain.Comment;
import com.gamesphere.discussions.domain.Discussion;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "content_reports")
public class ContentReport {

    public enum Status { PENDING, REVIEWED, DISMISSED }

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    protected ContentReport() {}

    public ContentReport(User reporter, Discussion discussion, Comment comment, String reason) {
        this.id = UUID.randomUUID();
        this.reporter = reporter;
        this.discussion = discussion;
        this.comment = comment;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public User getReporter() { return reporter; }
    public Discussion getDiscussion() { return discussion; }
    public Comment getComment() { return comment; }
    public String getReason() { return reason; }
    public Status getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }

    public void review(Status status) {
        this.status = status;
        this.reviewedAt = OffsetDateTime.now();
    }
}
