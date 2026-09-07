CREATE TABLE seller_ratings (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL,
    seller_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    rating SMALLINT NOT NULL,
    review TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_seller_ratings_listing
        FOREIGN KEY (listing_id) REFERENCES game_listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_seller_ratings_seller
        FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_seller_ratings_reviewer
        FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_seller_ratings_listing_reviewer UNIQUE (listing_id, reviewer_id),
    CONSTRAINT chk_seller_ratings_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_seller_ratings_different_users CHECK (seller_id <> reviewer_id)
);

CREATE INDEX idx_seller_ratings_seller_id ON seller_ratings(seller_id);
CREATE INDEX idx_seller_ratings_listing_id ON seller_ratings(listing_id);
CREATE INDEX idx_seller_ratings_reviewer_id ON seller_ratings(reviewer_id);
