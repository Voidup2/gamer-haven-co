package com.gamesphere.chat;

import java.io.Serializable;
import java.util.UUID;

public class ChatRoomMemberId implements Serializable {
    private UUID room;
    private Long user;

    public ChatRoomMemberId() {}

    public ChatRoomMemberId(UUID room, Long user) {
        this.room = room;
        this.user = user;
    }

    public UUID getRoom() { return room; }
    public Long getUser() { return user; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoomMemberId other)) return false;
        return java.util.Objects.equals(room, other.room) && java.util.Objects.equals(user, other.user);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(room, user); }
}
