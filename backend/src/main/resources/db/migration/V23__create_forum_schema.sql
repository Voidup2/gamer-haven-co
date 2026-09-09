CREATE TABLE forum_topics (
    id UUID PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    category VARCHAR(40) NOT NULL,
    content VARCHAR(5000) NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    game_id VARCHAR(100) REFERENCES games(id) ON DELETE SET NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    view_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_forum_topic_title CHECK (length(trim(title)) > 0),
    CONSTRAINT chk_forum_topic_content CHECK (length(trim(content)) > 0)
);

CREATE TABLE forum_posts (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL REFERENCES forum_topics(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content VARCHAR(5000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_forum_post_content CHECK (length(trim(content)) > 0)
);

CREATE INDEX idx_forum_topics_category ON forum_topics(category);
CREATE INDEX idx_forum_topics_author ON forum_topics(author_id);
CREATE INDEX idx_forum_topics_game ON forum_topics(game_id);
CREATE INDEX idx_forum_topics_latest ON forum_topics(updated_at DESC);
CREATE INDEX idx_forum_posts_topic_created ON forum_posts(topic_id, created_at ASC);
CREATE INDEX idx_forum_posts_author ON forum_posts(author_id);
