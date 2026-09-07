package com.gamesphere.activity.domain;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_activity")
public class UserActivity {
    public enum ActivityType { LIBRARY_ADDED, FAVORITE_ADDED, REVIEW_POSTED, DISCUSSION_CREATED, ACHIEVEMENT_UNLOCKED, GAME_COMPLETED, MARKETPLACE_PURCHASE, MARKETPLACE_SALE, COLLECTION_CREATED, COLLECTION_GAME_ADDED, PROGRESS_UPDATED }
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Enumerated(EnumType.STRING) @Column(name = "activity_type", nullable = false, length = 50) private ActivityType activityType;
    @Column(nullable = false, length = 200) private String title;
    @Column(length = 1000) private String description;
    @Column(name = "reference_type", length = 50) private String referenceType;
    @Column(name = "reference_id", length = 100) private String referenceId;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    protected UserActivity() {}
    public UserActivity(User user, ActivityType type, String title, String description, String referenceType, String referenceId) { this.id=UUID.randomUUID(); this.user=user; this.activityType=type; this.title=title; this.description=description; this.referenceType=referenceType; this.referenceId=referenceId; }
    @PrePersist void onCreate() { createdAt=OffsetDateTime.now(); }
    public UUID getId(){return id;} public User getUser(){return user;} public ActivityType getActivityType(){return activityType;} public String getTitle(){return title;} public String getDescription(){return description;} public String getReferenceType(){return referenceType;} public String getReferenceId(){return referenceId;} public OffsetDateTime getCreatedAt(){return createdAt;}
}