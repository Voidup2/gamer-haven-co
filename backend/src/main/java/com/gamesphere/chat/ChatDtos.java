package com.gamesphere.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ChatDtos {
    private ChatDtos() {}
    public record CreateDirectRequest(Long userId) {}
    public record SendMessageRequest(@NotBlank @Size(max = 2000) String content) {}
    public record WebSocketSendMessageRequest(@NotBlank String roomId, @NotBlank @Size(max = 2000) String content) {}
    public record EditMessageRequest(@NotBlank @Size(max = 2000) String content) {}
    public record RoomResponse(UUID id, ChatRoomType roomType, String roomKey, String name, String gameId, UUID groupId, long memberCount, OffsetDateTime createdAt) {}
    public record MessageResponse(UUID id, UUID roomId, Long senderId, String senderUsername, String senderDisplayName, String content, OffsetDateTime createdAt, OffsetDateTime editedAt) {}
    public record UnreadRoomResponse(UUID roomId, long unreadCount) {}
    public record UnreadTotalResponse(long unreadCount) {}
}
