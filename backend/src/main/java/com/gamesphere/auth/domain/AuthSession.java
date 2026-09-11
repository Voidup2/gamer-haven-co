package com.gamesphere.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
public class AuthSession {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64)
    private String refreshTokenHash;

    @Column(name = "remember_me", nullable = false)
    private boolean rememberMe;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_used_at", nullable = false)
    private OffsetDateTime lastUsedAt;

    protected AuthSession() {}

    public AuthSession(UUID id, User user, String refreshTokenHash, boolean rememberMe,
                       OffsetDateTime expiresAt, OffsetDateTime now) {
        this.id = id;
        this.user = user;
        this.refreshTokenHash = refreshTokenHash;
        this.rememberMe = rememberMe;
        this.expiresAt = expiresAt;
        this.createdAt = now;
        this.lastUsedAt = now;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public boolean isRememberMe() { return rememberMe; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public OffsetDateTime getRevokedAt() { return revokedAt; }
    public OffsetDateTime getLastUsedAt() { return lastUsedAt; }

    public void rotate(String refreshTokenHash, OffsetDateTime expiresAt, OffsetDateTime now) {
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.lastUsedAt = now;
    }

    public void revoke(OffsetDateTime now) {
        this.revokedAt = now;
    }

    public boolean isActive(OffsetDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
