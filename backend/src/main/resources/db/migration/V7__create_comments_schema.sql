CREATE TABLE comments (
    id UUID PRIMARY KEY,
    discussion_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_comments_discussion
        FOREIGN KEY (discussion_id)
        REFERENCES discussions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_comments_discussion_id
    ON comments(discussion_id);

CREATE INDEX idx_comments_user_id
    ON comments(user_id);

CREATE INDEX idx_comments_created_at
    ON comments(created_at);