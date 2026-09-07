package com.gamesphere.notifications.api;

import com.gamesphere.notifications.domain.NotificationPreferences;

public record NotificationPreferencesResponse(
        boolean marketplaceEnabled,
        boolean wishlistEnabled,
        boolean upcomingReleaseEnabled,
        boolean replyEnabled,
        boolean mentionEnabled
) {
    public static NotificationPreferencesResponse from(NotificationPreferences p) {
        return new NotificationPreferencesResponse(
                p.isMarketplaceEnabled(), p.isWishlistEnabled(), p.isUpcomingReleaseEnabled(),
                p.isReplyEnabled(), p.isMentionEnabled());
    }
}
