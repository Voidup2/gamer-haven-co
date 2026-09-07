package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.games.domain.Game;
import com.gamesphere.groups.GameGroup;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Column(name = "room_key", nullable = false, unique = true, length = 255)
    private String roomKey;

    @Column(length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GameGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ChatRoom() {}

    public ChatRoom(UUID id, ChatRoomType roomType, String roomKey, String name, Game game, GameGroup group, User createdBy) {
        this.id = id;
        this.roomType = roomType;
        this.roomKey = roomKey;
        this.name = name;
        this.game = game;
        this.group = group;
        this.createdBy = createdBy;
    }

    @PrePersist
    void onCreate() { createdAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public ChatRoomType getRoomType() { return roomType; }
    public String getRoomKey() { return roomKey; }
    public String getName() { return name; }
    public Game getGame() { return game; }
    public GameGroup getGroup() { return group; }
    public User getCreatedBy() { return createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
