CREATE TABLE game_achievements (
    id UUID PRIMARY KEY,
    game_id VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    points INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_achievements_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT uq_achievements_game_name UNIQUE (game_id, name),
    CONSTRAINT chk_achievements_points CHECK (points >= 0)
);

CREATE TABLE user_game_achievements (
    id UUID PRIMARY KEY,
    achievement_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    progress_percent INTEGER NOT NULL DEFAULT 0,
    unlocked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES game_achievements(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_achievement UNIQUE (achievement_id, user_id),
    CONSTRAINT chk_user_achievement_progress CHECK (progress_percent BETWEEN 0 AND 100)
);

CREATE INDEX idx_game_achievements_game_id ON game_achievements(game_id);
CREATE INDEX idx_user_game_achievements_user_id ON user_game_achievements(user_id);
CREATE INDEX idx_user_game_achievements_achievement_id ON user_game_achievements(achievement_id);
CREATE INDEX idx_user_game_achievements_unlocked ON user_game_achievements(user_id, unlocked_at);