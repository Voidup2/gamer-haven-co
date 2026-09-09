package com.gamesphere.forum;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "forum_topics")
public class ForumTopic {
    @Id
    private UUID id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 5000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "game_id", length = 100)
    private String gameId;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false)
    private boolean locked;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ForumTopic() {}

    public ForumTopic(String title, String category, String content, User author, String gameId) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.category = category;
        this.content = content;
        this.author = author;
        this.gameId = gameId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public User getAuthor() { return author; }
    public String getGameId() { return gameId; }
    public boolean isPinned() { return pinned; }
    public boolean isLocked() { return locked; }
    public long getViewCount() { return viewCount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(String title, String category, String content, String gameId) {
        this.title = title;
        this.category = category;
        this.content = content;
        this.gameId = gameId;
    }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public void incrementViews() { this.viewCount++; }
}
