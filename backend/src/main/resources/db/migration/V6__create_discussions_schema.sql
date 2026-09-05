CREATE TABLE discussions (
    id UUID PRIMARY KEY,
    game_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_discussions_game
        FOREIGN KEY (game_id)
        REFERENCES games(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_discussions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_discussions_game_id
    ON discussions(game_id);

CREATE INDEX idx_discussions_user_id
    ON discussions(user_id);

CREATE INDEX idx_discussions_created_at
    ON discussions(created_at);