package com.gamesphere.blocks.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_blocks")
@IdClass(UserBlockId.class)
public class UserBlock {
    @Id @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;
    @Id @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    protected UserBlock() {}
    public UserBlock(User blocker, User blocked) { this.blocker = blocker; this.blocked = blocked; }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
    public User getBlocker() { return blocker; }
    public User getBlocked() { return blocked; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
