package com.gamesphere.marketplace.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "marketplace_transactions")
public class MarketplaceTransaction {
    public enum Status { PENDING, COMPLETED, CANCELLED }

    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private GameListing listing;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected MarketplaceTransaction() {}

    public MarketplaceTransaction(GameListing listing, User buyer, User seller, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.listing = listing;
        this.buyer = buyer;
        this.seller = seller;
        this.amount = amount;
        this.status = Status.PENDING;
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
    public User getBuyer() { return buyer; }
    public User getSeller() { return seller; }
    public BigDecimal getAmount() { return amount; }
    public Status getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setStatus(Status status) { this.status = status; }
}