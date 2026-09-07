CREATE TABLE game_progress (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    playtime_minutes INTEGER NOT NULL DEFAULT 0,
    progress_percent INTEGER NOT NULL DEFAULT 0,
    notes VARCHAR(5000),
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_played_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_game_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_progress_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT uq_game_progress_user_game UNIQUE (user_id, game_id),
    CONSTRAINT chk_game_progress_status CHECK (status IN ('NOT_STARTED','PLAYING','COMPLETED','DROPPED','ON_HOLD')),
    CONSTRAINT chk_game_progress_playtime CHECK (playtime_minutes >= 0),
    CONSTRAINT chk_game_progress_percent CHECK (progress_percent BETWEEN 0 AND 100)
);

CREATE INDEX idx_game_progress_user_id ON game_progress(user_id);
CREATE INDEX idx_game_progress_game_id ON game_progress(game_id);
CREATE INDEX idx_game_progress_status ON game_progress(status);
CREATE INDEX idx_game_progress_last_played ON game_progress(user_id, last_played_at);