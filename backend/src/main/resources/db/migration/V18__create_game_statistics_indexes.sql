CREATE INDEX idx_reviews_game_id ON reviews(game_id);
CREATE INDEX idx_library_game_id ON user_game_library(game_id);
CREATE INDEX idx_favorites_game_id ON user_game_favorites(game_id);
CREATE INDEX idx_game_progress_game_id ON game_progress(game_id);
