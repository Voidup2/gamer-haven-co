package com.gamesphere.chat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatWebSocketControllerTest {
    private final ChatService chatService = mock(ChatService.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final ChatWebSocketController controller =
            new ChatWebSocketController(chatService, messagingTemplate);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendPublishesToRoomEventsTopicAndClearsSecurityContext() {
        UUID roomId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        var request = new ChatDtos.WebSocketSendMessageRequest(roomId.toString(), "hello");
        var response = mock(ChatDtos.MessageResponse.class);
        when(chatService.send(roomId, new ChatDtos.SendMessageRequest("hello"))).thenReturn(response);

        controller.send(request, authentication);

        verify(chatService).send(roomId, new ChatDtos.SendMessageRequest("hello"));
        verify(messagingTemplate).convertAndSend("/topic/chat/" + roomId + "/events", response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void sendRejectsNonAuthenticationPrincipal() {
        UUID roomId = UUID.randomUUID();
        var request = new ChatDtos.WebSocketSendMessageRequest(roomId.toString(), "hello");

        assertThrows(IllegalArgumentException.class, () -> controller.send(request, () -> "alice"));

        verifyNoInteractions(chatService, messagingTemplate);
    }

    @Test
    void sendClearsSecurityContextWhenServiceFails() {
        UUID roomId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        var request = new ChatDtos.WebSocketSendMessageRequest(roomId.toString(), "hello");
        when(chatService.send(roomId, new ChatDtos.SendMessageRequest("hello")))
                .thenThrow(new IllegalArgumentException("not a member"));

        assertThrows(IllegalArgumentException.class, () -> controller.send(request, authentication));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(messagingTemplate);
    }
}
