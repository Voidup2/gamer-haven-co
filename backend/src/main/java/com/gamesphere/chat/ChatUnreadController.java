package com.gamesphere.chat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/unread")
public class ChatUnreadController {
    private final ChatUnreadService unreadService;

    public ChatUnreadController(ChatUnreadService unreadService) {
        this.unreadService = unreadService;
    }

    @GetMapping
    public List<ChatDtos.UnreadRoomResponse> rooms() { return unreadService.rooms(); }

    @GetMapping("/total")
    public ChatDtos.UnreadTotalResponse total() { return unreadService.total(); }

    @GetMapping("/rooms/{roomId}")
    public ChatDtos.UnreadRoomResponse room(@PathVariable UUID roomId) {
        return new ChatDtos.UnreadRoomResponse(roomId, unreadService.count(roomId));
    }
}
