package com.gamesphere.notifications.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    public enum NotificationType {
        SYSTEM,
        REPLY,
        MENTION,
        MARKETPLACE,
        WISHLIST,
        UPCOMING_RELEASE
    }

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Notification() {}

    public Notification(User user, NotificationType type, String title, String message,
                        String referenceType, String referenceId) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceId() { return referenceId; }
    public boolean isRead() { return read; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void markRead() { this.read = true; }
}
