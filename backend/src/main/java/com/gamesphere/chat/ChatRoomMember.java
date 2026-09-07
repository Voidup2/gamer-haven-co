package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_room_members")
@IdClass(ChatRoomMemberId.class)
public class ChatRoomMember {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "last_read_at")
    private OffsetDateTime lastReadAt;

    protected ChatRoomMember() {}

    public ChatRoomMember(ChatRoom room, User user) {
        this.room = room;
        this.user = user;
    }

    @PrePersist
    void onCreate() { joinedAt = OffsetDateTime.now(); }

    public ChatRoom getRoom() { return room; }
    public User getUser() { return user; }
    public OffsetDateTime getJoinedAt() { return joinedAt; }
    public OffsetDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(OffsetDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}
