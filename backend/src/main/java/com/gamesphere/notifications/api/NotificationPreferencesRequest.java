package com.gamesphere.notifications.api;

public record NotificationPreferencesRequest(
        boolean marketplaceEnabled,
        boolean wishlistEnabled,
        boolean upcomingReleaseEnabled,
        boolean replyEnabled,
        boolean mentionEnabled
) {}
