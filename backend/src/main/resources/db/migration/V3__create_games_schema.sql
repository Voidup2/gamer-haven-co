CREATE TABLE games (
    id VARCHAR(100) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    tagline VARCHAR(300),
    description TEXT NOT NULL,
    cover_url TEXT,
    banner_url TEXT,
    rating NUMERIC(3,1) NOT NULL DEFAULT 0,
    review_count INTEGER NOT NULL DEFAULT 0,
    price NUMERIC(10,2) NOT NULL DEFAULT 0,
    discount INTEGER,
    release_date DATE,
    release_year INTEGER,
    developer VARCHAR(150),
    publisher VARCHAR(150),
    esrb VARCHAR(50),
    multiplayer BOOLEAN NOT NULL DEFAULT FALSE,
    coop BOOLEAN NOT NULL DEFAULT FALSE,
    free_to_play BOOLEAN NOT NULL DEFAULT FALSE,
    vr BOOLEAN NOT NULL DEFAULT FALSE,
    early_access BOOLEAN NOT NULL DEFAULT FALSE,
    controller BOOLEAN NOT NULL DEFAULT FALSE,
    genres JSONB NOT NULL DEFAULT '[]'::jsonb,
    platforms JSONB NOT NULL DEFAULT '[]'::jsonb,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    languages JSONB NOT NULL DEFAULT '[]'::jsonb,
    features JSONB NOT NULL DEFAULT '[]'::jsonb,
    stores JSONB NOT NULL DEFAULT '[]'::jsonb,
    requirements JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_games_title ON games(title);
CREATE INDEX idx_games_release_date ON games(release_date);
CREATE INDEX idx_games_developer ON games(developer);
