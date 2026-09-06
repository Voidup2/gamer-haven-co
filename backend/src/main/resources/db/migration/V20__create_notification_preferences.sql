CREATE TABLE notification_preferences (
    user_id BIGINT PRIMARY KEY,
    marketplace_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    wishlist_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    upcoming_release_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reply_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    mention_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_notification_preferences_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
