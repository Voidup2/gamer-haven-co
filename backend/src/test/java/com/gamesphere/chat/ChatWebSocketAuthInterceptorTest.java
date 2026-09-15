package com.gamesphere.chat;

import com.gamesphere.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatWebSocketAuthInterceptorTest {
    private final JwtService jwtService = mock(JwtService.class);
    private final MessageChannel channel = mock(MessageChannel.class);
    private final ChatWebSocketAuthInterceptor interceptor = new ChatWebSocketAuthInterceptor(jwtService);

    @Test
    void connectRejectsMissingAuthorization() {
        Message<?> message = stompMessage(StompCommand.CONNECT, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> interceptor.preSend(message, channel));

        assertEquals("WebSocket authentication required", exception.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    void connectRejectsBlankBearerToken() {
        Message<?> message = stompMessage(StompCommand.CONNECT, "Bearer   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> interceptor.preSend(message, channel));

        assertEquals("WebSocket authentication required", exception.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    void connectAuthenticatesUserFromJwtSubject() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("alice");
        when(jwtService.parse("token")).thenReturn(claims);

        Message<?> result = interceptor.preSend(
                stompMessage(StompCommand.CONNECT, "Bearer token"), channel);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        assertNotNull(accessor.getUser());
        assertEquals("alice", accessor.getUser().getName());
        verify(jwtService).parse("token");
    }

    @Test
    void connectRejectsTokenWithoutSubject() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(" ");
        when(jwtService.parse("token")).thenReturn(claims);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> interceptor.preSend(stompMessage(StompCommand.CONNECT, "Bearer token"), channel));

        assertEquals("Invalid WebSocket token", exception.getMessage());
    }

    @Test
    void nonConnectMessagePassesThroughWithoutJwtParsing() {
        Message<?> message = stompMessage(StompCommand.SEND, null);

        assertSame(message, interceptor.preSend(message, channel));
        verifyNoInteractions(jwtService);
    }

    private Message<byte[]> stompMessage(StompCommand command, String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (authorization != null) {
            accessor.addNativeHeader("Authorization", authorization);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
