package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ChatRealtimeController {
    private final UserRepository userRepository;
    private final ChatService chatService;

    public ChatRealtimeController(UserRepository userRepository, ChatService chatService) {
        this.userRepository = userRepository;
        this.chatService = chatService;
    }

    @MessageMapping("chat.typing")
    @SendTo("/topic/chat/typing")
    public ChatTypingEvent typing(ChatTypingRequest request) {
        User user = currentUser();
        chatService.assertCanAccess(request.roomId(), user);
        return new ChatTypingEvent(request.roomId(), user.getId(), user.getUsername(), request.typing());
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public record ChatTypingRequest(UUID roomId, boolean typing) {}
}
