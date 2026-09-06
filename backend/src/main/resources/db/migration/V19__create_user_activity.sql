CREATE TABLE user_activity (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    reference_type VARCHAR(50),
    reference_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_user_activity_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_activity_user_created ON user_activity(user_id, created_at);
CREATE INDEX idx_user_activity_type ON user_activity(activity_type);