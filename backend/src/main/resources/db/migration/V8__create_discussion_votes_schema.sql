CREATE TABLE discussion_votes (
    id UUID PRIMARY KEY,
    discussion_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_discussion_votes_discussion
        FOREIGN KEY (discussion_id)
        REFERENCES discussions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_discussion_votes_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_discussion_votes_user_discussion
        UNIQUE (discussion_id, user_id),

    CONSTRAINT chk_discussion_votes_type
        CHECK (vote_type IN ('UPVOTE', 'DOWNVOTE'))
);

CREATE INDEX idx_discussion_votes_discussion_id
    ON discussion_votes(discussion_id);

CREATE INDEX idx_discussion_votes_user_id
    ON discussion_votes(user_id);
