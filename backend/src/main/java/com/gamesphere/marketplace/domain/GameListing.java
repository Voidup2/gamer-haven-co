package com.gamesphere.marketplace.domain;

import com.gamesphere.auth.domain.User;
import com.gamesphere.games.domain.Game;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_listings")
public class GameListing {

    public enum Condition { LIKE_NEW, VERY_GOOD, GOOD, ACCEPTABLE, DAMAGED }
    public enum Status { ACTIVE, SOLD, REMOVED }

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Condition condition;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(length = 200)
    private String location;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "box_included", nullable = false)
    private boolean boxIncluded;

    @Column(name = "manual_included", nullable = false)
    private boolean manualIncluded;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected GameListing() {}

    public GameListing(Game game, User seller, String title, String imageUrl, String description,
                       Condition condition, BigDecimal price, String platform, String location,
                       String contactEmail, String contactPhone, boolean boxIncluded, boolean manualIncluded) {
        this.id = UUID.randomUUID();
        this.game = game;
        this.seller = seller;
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.condition = condition;
        this.price = price;
        this.platform = platform;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.boxIncluded = boxIncluded;
        this.manualIncluded = manualIncluded;
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
    public Game getGame() { return game; }
    public User getSeller() { return seller; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public Condition getCondition() { return condition; }
    public BigDecimal getPrice() { return price; }
    public String getPlatform() { return platform; }
    public String getLocation() { return location; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public boolean isBoxIncluded() { return boxIncluded; }
    public boolean isManualIncluded() { return manualIncluded; }
    public Status getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(String title, String imageUrl, String description, Condition condition,
                       BigDecimal price, String platform, String location, String contactEmail,
                       String contactPhone, boolean boxIncluded, boolean manualIncluded) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.condition = condition;
        this.price = price;
        this.platform = platform;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.boxIncluded = boxIncluded;
        this.manualIncluded = manualIncluded;
    }

    public void setStatus(Status status) { this.status = status; }
}
