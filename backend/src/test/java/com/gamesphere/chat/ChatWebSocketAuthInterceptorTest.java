package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketAuthInterceptorTest {
    @Mock JwtService jwtService;
    @Mock Claims claims;
    @Mock UserRepository userRepository;
    @Mock ChatService chatService;
    @Mock User user;

    @Test
    void connectRejectsMissingAuthorization() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        Message<byte[]> message = connectMessage(null);

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(message, null));
        verifyNoInteractions(jwtService);
    }

    @Test
    void connectRejectsBlankBearerToken() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        Message<byte[]> message = connectMessage("Bearer   ");

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(message, null));
        verifyNoInteractions(jwtService);
    }

    @Test
    void connectRejectsTokenWithoutSubject() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        when(jwtService.parse("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> interceptor.preSend(connectMessage("Bearer token"), null));
    }

    @Test
    void connectSetsAuthenticatedPrincipalFromJwtSubject() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        when(jwtService.parse("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("alice");

        Message<?> result = interceptor.preSend(connectMessage("Bearer token"), null);
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);

        assertNotNull(accessor.getUser());
        assertEquals("alice", accessor.getUser().getName());
        verify(jwtService).parse("token");
    }

    @Test
    void subscribeToChatRoomAuthorizesAuthenticatedUser() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        UUID roomId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken("alice", null);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/chat/" + roomId + "/events");
        accessor.setUser(authentication);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertSame(message, interceptor.preSend(message, null));
        verify(chatService).assertCanAccess(roomId, user);
    }

    @Test
    void subscribeToUnauthorizedChatRoomIsRejected() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        UUID roomId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken("alice", null);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/chat/" + roomId + "/events");
        accessor.setUser(authentication);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        doThrow(new AccessDeniedException("denied")).when(chatService).assertCanAccess(roomId, user);

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    void subscribeToMalformedChatDestinationIsRejected() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        var authentication = new UsernamePasswordAuthenticationToken("alice", null);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/chat/not-a-room/events");
        accessor.setUser(authentication);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
        verifyNoInteractions(userRepository, chatService);
    }

    @Test
    void nonConnectMessagePassesThroughWithoutAuthenticationParsing() {
        ChatWebSocketAuthInterceptor interceptor = interceptor();
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertSame(message, interceptor.preSend(message, null));
        verifyNoInteractions(jwtService, userRepository, chatService);
    }

    private ChatWebSocketAuthInterceptor interceptor() {
        return new ChatWebSocketAuthInterceptor(jwtService, userRepository, chatService);
    }

    private Message<byte[]> connectMessage(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
