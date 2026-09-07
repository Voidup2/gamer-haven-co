CREATE TABLE game_collections (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_collections_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_collections_user_name UNIQUE (user_id, name)
);

CREATE TABLE game_collection_items (
    id BIGSERIAL PRIMARY KEY,
    collection_id UUID NOT NULL,
    game_id VARCHAR(100) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_collection_items_collection FOREIGN KEY (collection_id) REFERENCES game_collections(id) ON DELETE CASCADE,
    CONSTRAINT fk_collection_items_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT uq_collection_items_collection_game UNIQUE (collection_id, game_id)
);

CREATE INDEX idx_game_collections_user_id ON game_collections(user_id);
CREATE INDEX idx_game_collections_public ON game_collections(is_public);
CREATE INDEX idx_collection_items_collection_id ON game_collection_items(collection_id);
CREATE INDEX idx_collection_items_game_id ON game_collection_items(game_id);