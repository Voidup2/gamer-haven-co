package com.gamesphere.marketplace.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "seller_ratings", uniqueConstraints = @UniqueConstraint(name = "uq_seller_ratings_listing_reviewer", columnNames = {"listing_id", "reviewer_id"}))
public class SellerRating {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private GameListing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(nullable = false)
    private short rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SellerRating() {}

    public SellerRating(GameListing listing, User seller, User reviewer, short rating, String review) {
        this.id = UUID.randomUUID();
        this.listing = listing;
        this.seller = seller;
        this.reviewer = reviewer;
        this.rating = rating;
        this.review = review;
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
    public GameListing getListing() { return listing; }
    public User getSeller() { return seller; }
    public User getReviewer() { return reviewer; }
    public short getRating() { return rating; }
    public String getReview() { return review; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(short rating, String review) {
        this.rating = rating;
        this.review = review;
    }
}
