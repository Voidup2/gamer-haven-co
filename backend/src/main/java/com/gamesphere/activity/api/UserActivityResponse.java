package com.gamesphere.activity.api;

import com.gamesphere.activity.domain.UserActivity;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserActivityResponse(UUID id, UserActivity.ActivityType activityType, String title, String description, String referenceType, String referenceId, OffsetDateTime createdAt) {
    public static UserActivityResponse from(UserActivity a) { return new UserActivityResponse(a.getId(), a.getActivityType(), a.getTitle(), a.getDescription(), a.getReferenceType(), a.getReferenceId(), a.getCreatedAt()); }
}