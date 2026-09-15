package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRealtimeControllerTest {
    @Mock UserRepository userRepository;
    @Mock ChatService chatService;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock User user;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void typingRequiresAuthenticatedPrincipal() {
        ChatRealtimeController controller = new ChatRealtimeController(userRepository, chatService, messagingTemplate);
        UUID roomId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class,
                () -> controller.typing(new ChatRealtimeController.ChatTypingRequest(roomId, true), null));
        verifyNoInteractions(userRepository, chatService, messagingTemplate);
    }

    @Test
    void typingRequiresRoomAccessAndPublishesOnlyToRoomTopic() {
        ChatRealtimeController controller = new ChatRealtimeController(userRepository, chatService, messagingTemplate);
        UUID roomId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("alice");

        controller.typing(new ChatRealtimeController.ChatTypingRequest(roomId, true), authentication);

        verify(chatService).assertCanAccess(roomId, user);
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/" + roomId + "/typing"), any(ChatTypingEvent.class));
        verifyNoMoreInteractions(messagingTemplate);
        org.junit.jupiter.api.Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void typingDoesNotPublishWhenRoomAccessIsDenied() {
        ChatRealtimeController controller = new ChatRealtimeController(userRepository, chatService, messagingTemplate);
        UUID roomId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        doThrow(new AccessDeniedException("Chat access denied"))
                .when(chatService).assertCanAccess(roomId, user);

        assertThrows(AccessDeniedException.class,
                () -> controller.typing(new ChatRealtimeController.ChatTypingRequest(roomId, true), authentication));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any());
    }
}
