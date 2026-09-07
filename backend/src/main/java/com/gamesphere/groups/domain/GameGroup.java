package com.gamesphere.groups.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "groups", uniqueConstraints = @UniqueConstraint(name = "uq_groups_owner_name", columnNames = {"owner_id", "name"}))
public class GameGroup {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url", length = 2000)
    private String imageUrl;

    @Column(name = "is_public", nullable = false)
    private boolean publicGroup;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected GameGroup() {}

    public GameGroup(User owner, String name, String description, String imageUrl, boolean publicGroup) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.publicGroup = publicGroup;
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
    public User getOwner() { return owner; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public boolean isPublicGroup() { return publicGroup; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(String name, String description, String imageUrl, boolean publicGroup) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.publicGroup = publicGroup;
    }
}
