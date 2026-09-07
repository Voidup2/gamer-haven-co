package com.gamesphere.chat;

import com.gamesphere.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ChatWebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;

    public ChatWebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new IllegalArgumentException("WebSocket authentication required");
            }
            String token = authorization.substring(7).trim();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("WebSocket authentication required");
            }
            Claims claims = jwtService.parse(token);
            String username = claims.getSubject();
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("Invalid WebSocket token");
            }
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, java.util.List.of());
            accessor.setUser(authentication);
        }
        return message;
    }
}
