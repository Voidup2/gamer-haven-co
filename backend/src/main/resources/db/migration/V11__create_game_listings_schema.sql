CREATE TABLE game_listings (
    id UUID PRIMARY KEY,
    game_id VARCHAR(100) NOT NULL,
    seller_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    image_url TEXT,
    description TEXT NOT NULL,
    condition VARCHAR(30) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    location VARCHAR(200),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(30),
    box_included BOOLEAN NOT NULL DEFAULT FALSE,
    manual_included BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_game_listings_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_listings_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_game_listings_condition CHECK (condition IN ('LIKE_NEW', 'VERY_GOOD', 'GOOD', 'ACCEPTABLE', 'DAMAGED')),
    CONSTRAINT chk_game_listings_status CHECK (status IN ('ACTIVE', 'SOLD', 'REMOVED')),
    CONSTRAINT chk_game_listings_price CHECK (price >= 0)
);

CREATE INDEX idx_game_listings_game_id ON game_listings(game_id);
CREATE INDEX idx_game_listings_seller_id ON game_listings(seller_id);
CREATE INDEX idx_game_listings_status ON game_listings(status);
CREATE INDEX idx_game_listings_platform ON game_listings(platform);
CREATE INDEX idx_game_listings_created_at ON game_listings(created_at);
