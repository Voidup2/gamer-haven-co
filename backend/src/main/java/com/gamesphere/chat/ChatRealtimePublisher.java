package com.gamesphere.chat;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class ChatRealtimePublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void messageEdited(ChatDtos.MessageResponse message) {
        publish(new ChatRealtimeEvent(ChatRealtimeEventType.MESSAGE_EDITED, message.roomId(), message.id(),
                message.senderId(), message.senderUsername(), OffsetDateTime.now()));
    }

    public void messageDeleted(UUID roomId, UUID messageId, Long userId, String username) {
        publish(new ChatRealtimeEvent(ChatRealtimeEventType.MESSAGE_DELETED, roomId, messageId,
                userId, username, OffsetDateTime.now()));
    }

    public void read(UUID roomId, Long userId, String username) {
        publish(new ChatRealtimeEvent(ChatRealtimeEventType.READ_RECEIPT, roomId, null,
                userId, username, OffsetDateTime.now()));
    }

    public void roomMemberChanged(ChatRealtimeEventType type, UUID roomId, Long userId, String username) {
        publish(new ChatRealtimeEvent(type, roomId, null, userId, username, OffsetDateTime.now()));
    }

    private void publish(ChatRealtimeEvent event) {
        messagingTemplate.convertAndSend("/topic/chat/" + event.roomId() + "/events", event);
    }
}
