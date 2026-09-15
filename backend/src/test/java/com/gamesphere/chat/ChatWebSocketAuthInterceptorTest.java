package com.gamesphere.chat;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketAuthInterceptorTest {
    @Mock JwtService jwtService;
    @Mock Claims claims;

    @Test
    void connectRejectsMissingAuthorization() {
        ChatWebSocketAuthInterceptor interceptor = new ChatWebSocketAuthInterceptor(jwtService);
        Message<byte[]> message = connectMessage(null);

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(message, null));
        verifyNoInteractions(jwtService);
    }

    @Test
    void connectRejectsBlankBearerToken() {
        ChatWebSocketAuthInterceptor interceptor = new ChatWebSocketAuthInterceptor(jwtService);
        Message<byte[]> message = connectMessage("Bearer   ");

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(message, null));
        verifyNoInteractions(jwtService);
    }

    @Test
    void connectRejectsTokenWithoutSubject() {
        ChatWebSocketAuthInterceptor interceptor = new ChatWebSocketAuthInterceptor(jwtService);
        when(jwtService.parse("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> interceptor.preSend(connectMessage("Bearer token"), null));
    }

    @Test
    void connectSetsAuthenticatedPrincipalFromJwtSubject() {
        ChatWebSocketAuthInterceptor interceptor = new ChatWebSocketAuthInterceptor(jwtService);
        when(jwtService.parse("token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("alice");

        Message<byte[]> result = interceptor.preSend(connectMessage("Bearer token"), null);
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);

        assertNotNull(accessor.getUser());
        assertEquals("alice", accessor.getUser().getName());
        verify(jwtService).parse("token");
    }

    @Test
    void nonConnectMessagePassesThroughWithoutAuthenticationParsing() {
        ChatWebSocketAuthInterceptor interceptor = new ChatWebSocketAuthInterceptor(jwtService);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertSame(message, interceptor.preSend(message, null));
        verifyNoInteractions(jwtService);
    }

    private Message<byte[]> connectMessage(String authorization) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
