package com.gamesphere.discussions.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "discussion_votes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_discussion_votes_user_discussion",
                columnNames = {"discussion_id", "user_id"}
        )
)
public class DiscussionVote {

    public enum VoteType {
        UPVOTE,
        DOWNVOTE
    }

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 20)
    private VoteType voteType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected DiscussionVote() {}

    public DiscussionVote(Discussion discussion, User user, VoteType voteType) {
        this.id = UUID.randomUUID();
        this.discussion = discussion;
        this.user = user;
        this.voteType = voteType;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public User getUser() {
        return user;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
}
