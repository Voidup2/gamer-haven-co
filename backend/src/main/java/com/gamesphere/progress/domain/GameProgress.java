package com.gamesphere.progress.domain;

import com.gamesphere.auth.domain.User;
import com.gamesphere.games.domain.Game;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_progress", uniqueConstraints = @UniqueConstraint(name = "uq_game_progress_user_game", columnNames = {"user_id", "game_id"}))
public class GameProgress {
    public enum Status { NOT_STARTED, PLAYING, COMPLETED, DROPPED, ON_HOLD }

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "playtime_minutes", nullable = false)
    private int playtimeMinutes;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(length = 5000)
    private String notes;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "last_played_at")
    private OffsetDateTime lastPlayedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected GameProgress() {}

    public GameProgress(User user, Game game, Status status, int playtimeMinutes, int progressPercent, String notes) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.game = game;
        this.status = status;
        this.playtimeMinutes = playtimeMinutes;
        this.progressPercent = progressPercent;
        this.notes = notes;
        if (status == Status.PLAYING || status == Status.COMPLETED) this.startedAt = OffsetDateTime.now();
        if (status == Status.COMPLETED) {
            this.progressPercent = 100;
            this.completedAt = OffsetDateTime.now();
        }
        if (status == Status.PLAYING) this.lastPlayedAt = OffsetDateTime.now();
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
    public User getUser() { return user; }
    public Game getGame() { return game; }
    public Status getStatus() { return status; }
    public int getPlaytimeMinutes() { return playtimeMinutes; }
    public int getProgressPercent() { return progressPercent; }
    public String getNotes() { return notes; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public OffsetDateTime getLastPlayedAt() { return lastPlayedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(Status status, int playtimeMinutes, int progressPercent, String notes) {
        this.status = status;
        this.playtimeMinutes = playtimeMinutes;
        this.progressPercent = progressPercent;
        this.notes = notes;
        OffsetDateTime now = OffsetDateTime.now();
        if ((status == Status.PLAYING || status == Status.COMPLETED) && startedAt == null) startedAt = now;
        if (status == Status.PLAYING) lastPlayedAt = now;
        if (status == Status.COMPLETED) {
            this.progressPercent = 100;
            if (completedAt == null) completedAt = now;
        } else {
            completedAt = null;
        }
    }
}