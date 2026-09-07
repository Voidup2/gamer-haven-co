CREATE TABLE user_game_favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_favorites_game
        FOREIGN KEY (game_id)
        REFERENCES games(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_favorites_user_game
        UNIQUE (user_id, game_id)
);

CREATE INDEX idx_favorites_user_id ON user_game_favorites(user_id);
CREATE INDEX idx_favorites_game_id ON user_game_favorites(game_id);
