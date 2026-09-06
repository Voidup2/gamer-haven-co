package com.gamesphere.achievements.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_game_achievements", uniqueConstraints = @UniqueConstraint(name = "uq_user_achievement", columnNames = {"achievement_id", "user_id"}))
public class UserGameAchievement {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "achievement_id", nullable = false) private GameAchievement achievement;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "progress_percent", nullable = false) private int progressPercent;
    @Column(name = "unlocked_at") private OffsetDateTime unlockedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected UserGameAchievement() {}
    public UserGameAchievement(GameAchievement achievement, User user, int progressPercent) {
        this.id = UUID.randomUUID(); this.achievement = achievement; this.user = user; this.progressPercent = progressPercent;
        if (progressPercent == 100) this.unlockedAt = OffsetDateTime.now();
    }
    @PrePersist void onCreate() { OffsetDateTime now = OffsetDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(); }
    public UUID getId() { return id; }
    public GameAchievement getAchievement() { return achievement; }
    public User getUser() { return user; }
    public int getProgressPercent() { return progressPercent; }
    public OffsetDateTime getUnlockedAt() { return unlockedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void updateProgress(int progressPercent) { this.progressPercent = progressPercent; if (progressPercent == 100 && unlockedAt == null) unlockedAt = OffsetDateTime.now(); }
}