package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class ChatPresenceEventListener {
    private final UserRepository userRepository;
    private final ChatPresenceService presenceService;

    public ChatPresenceEventListener(UserRepository userRepository, ChatPresenceService presenceService) {
        this.userRepository = userRepository;
        this.presenceService = presenceService;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() == null) return;
        userRepository.findByUsername(accessor.getUser().getName()).ifPresent(this::online);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() == null) return;
        userRepository.findByUsername(event.getUser().getName()).ifPresent(this::offline);
    }

    private void online(User user) { presenceService.connected(user.getId(), user.getUsername(), user.getDisplayName()); }
    private void offline(User user) { presenceService.disconnected(user.getId(), user.getUsername(), user.getDisplayName()); }
}
