package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChatWebSocketAuthInterceptor implements ChannelInterceptor {
    private static final String CHAT_TOPIC_PREFIX = "/topic/chat/";
    private static final String EVENTS_SUFFIX = "/events";
    private static final String TYPING_SUFFIX = "/typing";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ChatService chatService;

    public ChatWebSocketAuthInterceptor(JwtService jwtService, UserRepository userRepository, ChatService chatService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.chatService = chatService;
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
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, java.util.List.of());
            accessor.setUser(authentication);
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeChatSubscription(accessor);
        }

        return message;
    }

    private void authorizeChatSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(CHAT_TOPIC_PREFIX)) {
            return;
        }

        String roomPath = destination.substring(CHAT_TOPIC_PREFIX.length());
        String roomIdText;
        if (roomPath.endsWith(EVENTS_SUFFIX)) {
            roomIdText = roomPath.substring(0, roomPath.length() - EVENTS_SUFFIX.length());
        } else if (roomPath.endsWith(TYPING_SUFFIX)) {
            roomIdText = roomPath.substring(0, roomPath.length() - TYPING_SUFFIX.length());
        } else {
            throw new AccessDeniedException("Invalid chat subscription destination");
        }

        UUID roomId;
        try {
            roomId = UUID.fromString(roomIdText);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Invalid chat room");
        }

        if (!(accessor.getUser() instanceof Authentication authentication)) {
            throw new AccessDeniedException("Authentication required");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
        chatService.assertCanAccess(roomId, user);
    }
}
