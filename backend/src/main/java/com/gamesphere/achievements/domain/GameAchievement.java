package com.gamesphere.achievements.domain;

import com.gamesphere.games.domain.Game;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_achievements", uniqueConstraints = @UniqueConstraint(name = "uq_achievements_game_name", columnNames = {"game_id", "name"}))
public class GameAchievement {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    @Column(nullable = false, length = 150) private String name;
    @Column(length = 1000) private String description;
    @Column(nullable = false) private int points;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;

    protected GameAchievement() {}
    public GameAchievement(Game game, String name, String description, int points) {
        this.id = UUID.randomUUID(); this.game = game; this.name = name; this.description = description; this.points = points;
    }
    @PrePersist void onCreate() { createdAt = OffsetDateTime.now(); }
    public UUID getId() { return id; }
    public Game getGame() { return game; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPoints() { return points; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void update(String name, String description, int points) { this.name = name; this.description = description; this.points = points; }
}