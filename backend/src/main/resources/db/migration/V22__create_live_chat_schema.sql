CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY,
    room_type VARCHAR(20) NOT NULL,
    room_key VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(120),
    game_id VARCHAR(100) REFERENCES games(id) ON DELETE CASCADE,
    group_id UUID REFERENCES groups(id) ON DELETE CASCADE,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_chat_room_type CHECK (room_type IN ('GLOBAL','GAME','TRADE','GROUP','DIRECT'))
);

CREATE TABLE chat_room_members (
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMPTZ,
    PRIMARY KEY (room_id, user_id)
);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_chat_message_content CHECK (length(trim(content)) > 0)
);

CREATE INDEX idx_chat_rooms_type ON chat_rooms(room_type);
CREATE INDEX idx_chat_rooms_game ON chat_rooms(game_id);
CREATE INDEX idx_chat_rooms_group ON chat_rooms(group_id);
CREATE INDEX idx_chat_room_members_user ON chat_room_members(user_id);
CREATE INDEX idx_chat_messages_room_created ON chat_messages(room_id, created_at DESC);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
