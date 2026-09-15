package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatRealtimeControllerTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ChatService chatService = mock(ChatService.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final ChatRealtimeController controller =
            new ChatRealtimeController(userRepository, chatService, messagingTemplate);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void typingPublishesOnlyToRoomTopicAfterAuthorization() {
        UUID roomId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        var authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        var request = new ChatRealtimeController.ChatTypingRequest(roomId, true);

        controller.typing(request, authentication);

        ChatTypingEvent expected = new ChatTypingEvent(roomId, 1L, "alice", true);
        verify(chatService).assertCanAccess(roomId, user);
        verify(messagingTemplate).convertAndSend("/topic/chat/" + roomId + "/typing", expected);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void typingRejectsNonAuthenticationPrincipal() {
        UUID roomId = UUID.randomUUID();
        var request = new ChatRealtimeController.ChatTypingRequest(roomId, true);

        assertThrows(AccessDeniedException.class, () -> controller.typing(request, () -> "alice"));

        verifyNoInteractions(userRepository, chatService, messagingTemplate);
    }

    @Test
    void typingRejectsUnknownAuthenticatedUser() {
        UUID roomId = UUID.randomUUID();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        var authentication = new UsernamePasswordAuthenticationToken("alice", null, List.of());
        var request = new ChatRealtimeController.ChatTypingRequest(roomId, true);

        assertThrows(AccessDeniedException.class, () -> controller.typing(request, authentication));

        verifyNoInteractions(chatService, messagingTemplate);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
