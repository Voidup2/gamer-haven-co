package com.gamesphere.chat;

import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatWebSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(@Payload @Valid ChatDtos.WebSocketSendMessageRequest request, Principal principal) {
        if (!(principal instanceof Authentication authentication)) {
            throw new IllegalArgumentException("Authentication required");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            UUID roomId = UUID.fromString(request.roomId());
            ChatDtos.MessageResponse response = chatService.send(roomId,
                    new ChatDtos.SendMessageRequest(request.content()));
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
