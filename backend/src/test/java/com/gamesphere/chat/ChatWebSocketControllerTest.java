package com.gamesphere.chat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {
    @Mock ChatService chatService;
    @Mock SimpMessagingTemplate messagingTemplate;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendRequiresAuthenticatedPrincipal() {
        ChatWebSocketController controller = new ChatWebSocketController(chatService, messagingTemplate);
        String roomId = UUID.randomUUID().toString();
        ChatDtos.WebSocketSendMessageRequest request =
                new ChatDtos.WebSocketSendMessageRequest(roomId, "hello");

        assertThrows(IllegalArgumentException.class, () -> controller.send(request, null));
        verifyNoInteractions(chatService, messagingTemplate);
    }

    @Test
    void sendPublishesToRoomEventsTopic() {
        ChatWebSocketController controller = new ChatWebSocketController(chatService, messagingTemplate);
        UUID roomId = UUID.randomUUID();
        ChatDtos.WebSocketSendMessageRequest request =
                new ChatDtos.WebSocketSendMessageRequest(roomId.toString(), "hello");
        ChatDtos.MessageResponse response = new ChatDtos.MessageResponse(
                UUID.randomUUID(), roomId, 1L, "alice", "Alice", "hello", null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        when(chatService.send(eq(roomId), eq(new ChatDtos.SendMessageRequest("hello"))))
                .thenReturn(response);

        controller.send(request, authentication);

        verify(chatService).send(eq(roomId), eq(new ChatDtos.SendMessageRequest("hello")));
        verify(messagingTemplate).convertAndSend("/topic/chat/" + roomId + "/events", response);
        org.junit.jupiter.api.Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void sendRejectsInvalidRoomId() {
        ChatWebSocketController controller = new ChatWebSocketController(chatService, messagingTemplate);
        ChatDtos.WebSocketSendMessageRequest request =
                new ChatDtos.WebSocketSendMessageRequest("not-a-uuid", "hello");
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());

        assertThrows(IllegalArgumentException.class, () -> controller.send(request, authentication));
        verifyNoInteractions(chatService, messagingTemplate);
    }
}
