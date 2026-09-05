package com.gamesphere.notifications.api;

import com.gamesphere.notifications.domain.Notification;
import com.gamesphere.notifications.domain.Notification.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        String referenceType,
        String referenceId,
        boolean read,
        OffsetDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
