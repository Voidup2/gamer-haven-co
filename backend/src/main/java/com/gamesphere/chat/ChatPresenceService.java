package com.gamesphere.chat;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ChatPresenceService {
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Long, AtomicInteger> sessions = new ConcurrentHashMap<>();

    public ChatPresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void connected(Long userId, String username, String displayName) {
        AtomicInteger count = sessions.computeIfAbsent(userId, ignored -> new AtomicInteger());
        if (count.incrementAndGet() == 1) publish(userId, username, displayName, ChatPresenceStatus.ONLINE);
    }

    public void disconnected(Long userId, String username, String displayName) {
        AtomicInteger count = sessions.get(userId);
        if (count == null) return;
        if (count.decrementAndGet() <= 0) {
            sessions.remove(userId);
            publish(userId, username, displayName, ChatPresenceStatus.OFFLINE);
        }
    }

    private void publish(Long userId, String username, String displayName, ChatPresenceStatus status) {
        messagingTemplate.convertAndSend("/topic/chat/presence", new ChatPresenceEvent(userId, username, displayName, status));
    }
}
