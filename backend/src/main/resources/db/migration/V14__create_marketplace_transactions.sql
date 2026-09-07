CREATE TABLE marketplace_transactions (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_transactions_listing FOREIGN KEY (listing_id)
        REFERENCES game_listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_buyer FOREIGN KEY (buyer_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_seller FOREIGN KEY (seller_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_transactions_amount CHECK (amount >= 0),
    CONSTRAINT chk_transactions_status CHECK (status IN ('PENDING','COMPLETED','CANCELLED')),
    CONSTRAINT uq_transactions_listing_buyer UNIQUE (listing_id, buyer_id)
);

CREATE INDEX idx_transactions_buyer_id ON marketplace_transactions(buyer_id);
CREATE INDEX idx_transactions_seller_id ON marketplace_transactions(seller_id);
CREATE INDEX idx_transactions_listing_id ON marketplace_transactions(listing_id);
CREATE INDEX idx_transactions_status ON marketplace_transactions(status);
CREATE INDEX idx_transactions_created_at ON marketplace_transactions(created_at);