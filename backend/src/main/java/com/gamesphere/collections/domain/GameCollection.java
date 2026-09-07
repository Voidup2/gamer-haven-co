package com.gamesphere.collections.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_collections", uniqueConstraints = @UniqueConstraint(name = "uq_collections_user_name", columnNames = {"user_id", "name"}))
public class GameCollection {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean publicCollection;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected GameCollection() {}

    public GameCollection(User user, String name, String description, boolean publicCollection) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.name = name;
        this.description = description;
        this.publicCollection = publicCollection;
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
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isPublicCollection() { return publicCollection; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(String name, String description, boolean publicCollection) {
        this.name = name;
        this.description = description;
        this.publicCollection = publicCollection;
    }
}