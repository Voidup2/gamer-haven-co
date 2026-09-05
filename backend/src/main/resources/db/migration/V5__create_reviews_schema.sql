CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    game_id VARCHAR(100) NOT NULL,

    rating NUMERIC(3,1) NOT NULL,

    title VARCHAR(200),

    content TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_reviews_game
        FOREIGN KEY (game_id)
        REFERENCES games(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_reviews_user_game
        UNIQUE (user_id, game_id),

    CONSTRAINT chk_review_rating
        CHECK (rating >= 0 AND rating <= 10)
);

CREATE INDEX idx_reviews_game_id
    ON reviews(game_id);

CREATE INDEX idx_reviews_user_id
    ON reviews(user_id);
    