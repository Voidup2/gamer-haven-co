package com.gamesphere.chat;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatUnreadService unreadService;

    public ChatController(ChatService chatService, ChatUnreadService unreadService) { this.chatService = chatService; this.unreadService = unreadService; }

    @GetMapping("/rooms") public Page<ChatDtos.RoomResponse> rooms(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { validatePage(page, size); return chatService.myRooms(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))); }
    @PostMapping("/rooms/global") public ChatDtos.RoomResponse global() { return chatService.global(); }
    @PostMapping("/rooms/trade") public ChatDtos.RoomResponse trade() { return chatService.trade(); }
    @PostMapping("/rooms/game/{gameId}") public ChatDtos.RoomResponse game(@PathVariable String gameId) { return chatService.game(gameId); }
    @PostMapping("/rooms/group/{groupId}") public ChatDtos.RoomResponse group(@PathVariable UUID groupId) { return chatService.group(groupId); }
    @PostMapping("/rooms/direct/{userId}") public ChatDtos.RoomResponse direct(@PathVariable Long userId) { return chatService.direct(userId); }
    @PostMapping("/rooms/{roomId}/join") public ChatDtos.RoomResponse join(@PathVariable UUID roomId) { return chatService.join(roomId); }
    @DeleteMapping("/rooms/{roomId}/leave") @ResponseStatus(HttpStatus.NO_CONTENT) public void leave(@PathVariable UUID roomId) { chatService.leave(roomId); }
    @GetMapping("/rooms/{roomId}/messages") public Page<ChatDtos.MessageResponse> messages(@PathVariable UUID roomId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) { validatePage(page, size); return chatService.messages(roomId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))); }
    @PostMapping("/rooms/{roomId}/messages") public ChatDtos.MessageResponse send(@PathVariable UUID roomId, @Valid @RequestBody ChatDtos.SendMessageRequest request) { return chatService.send(roomId, request); }
    @PutMapping("/messages/{messageId}") public ChatDtos.MessageResponse edit(@PathVariable UUID messageId, @Valid @RequestBody ChatDtos.EditMessageRequest request) { return chatService.edit(messageId, request); }
    @DeleteMapping("/messages/{messageId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID messageId) { chatService.delete(messageId); }
    @PostMapping("/rooms/{roomId}/read") @ResponseStatus(HttpStatus.NO_CONTENT) public void markRead(@PathVariable UUID roomId) { chatService.markRead(roomId); }

    @GetMapping("/unread") public List<ChatDtos.UnreadRoomResponse> unreadRooms() { return unreadService.rooms(); }
    @GetMapping("/unread/total") public ChatDtos.UnreadTotalResponse unreadTotal() { return unreadService.total(); }
    @GetMapping("/rooms/{roomId}/unread") public ChatDtos.UnreadRoomResponse unreadRoom(@PathVariable UUID roomId) { return new ChatDtos.UnreadRoomResponse(roomId, unreadService.count(roomId)); }

    private void validatePage(int page, int size) { if (page < 0) throw new IllegalArgumentException("page must be >= 0"); if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100"); }
}
