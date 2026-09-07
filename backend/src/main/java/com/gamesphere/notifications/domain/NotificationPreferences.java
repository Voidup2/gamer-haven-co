package com.gamesphere.notifications.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferences {
    @Id
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "marketplace_enabled", nullable = false)
    private boolean marketplaceEnabled = true;
    @Column(name = "wishlist_enabled", nullable = false)
    private boolean wishlistEnabled = true;
    @Column(name = "upcoming_release_enabled", nullable = false)
    private boolean upcomingReleaseEnabled = true;
    @Column(name = "reply_enabled", nullable = false)
    private boolean replyEnabled = true;
    @Column(name = "mention_enabled", nullable = false)
    private boolean mentionEnabled = true;

    protected NotificationPreferences() {}

    public NotificationPreferences(User user) { this.user = user; }

    public User getUser() { return user; }
    public boolean isMarketplaceEnabled() { return marketplaceEnabled; }
    public boolean isWishlistEnabled() { return wishlistEnabled; }
    public boolean isUpcomingReleaseEnabled() { return upcomingReleaseEnabled; }
    public boolean isReplyEnabled() { return replyEnabled; }
    public boolean isMentionEnabled() { return mentionEnabled; }

    public void update(boolean marketplaceEnabled, boolean wishlistEnabled,
                       boolean upcomingReleaseEnabled, boolean replyEnabled, boolean mentionEnabled) {
        this.marketplaceEnabled = marketplaceEnabled;
        this.wishlistEnabled = wishlistEnabled;
        this.upcomingReleaseEnabled = upcomingReleaseEnabled;
        this.replyEnabled = replyEnabled;
        this.mentionEnabled = mentionEnabled;
    }

    public boolean allows(Notification.NotificationType type) {
        return switch (type) {
            case MARKETPLACE -> marketplaceEnabled;
            case WISHLIST -> wishlistEnabled;
            case UPCOMING_RELEASE -> upcomingReleaseEnabled;
            case REPLY -> replyEnabled;
            case MENTION -> mentionEnabled;
            case SYSTEM -> true;
        };
    }
}
