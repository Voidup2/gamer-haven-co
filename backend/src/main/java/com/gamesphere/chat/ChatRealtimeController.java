package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatRealtimeController {
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRealtimeController(
            UserRepository userRepository,
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("chat.typing")
    public void typing(ChatTypingRequest request, Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            throw new AccessDeniedException("Authentication required");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            User user = currentUser(authentication);
            chatService.assertCanAccess(request.roomId(), user);
            ChatTypingEvent event = new ChatTypingEvent(
                    request.roomId(), user.getId(), user.getUsername(), request.typing());
            messagingTemplate.convertAndSend("/topic/chat/" + request.roomId() + "/typing", event);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    public record ChatTypingRequest(UUID roomId, boolean typing) {}
}
