package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.exception.ConflictException;
import com.gamesphere.common.exception.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.groups.domain.GameGroup;
import com.gamesphere.groups.repository.GameGroupRepository;
import com.gamesphere.groups.repository.GroupMemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ChatService {
    private final ChatRoomRepository roomRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ChatService(ChatRoomRepository roomRepository, ChatRoomMemberRepository memberRepository,
                       ChatMessageRepository messageRepository, UserRepository userRepository,
                       GameRepository gameRepository, GameGroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository) {
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public ChatDtos.RoomResponse global() { return createOrJoin(ChatRoomType.GLOBAL, "global", "Global Chat", null, null); }

    @Transactional
    public ChatDtos.RoomResponse trade() { return createOrJoin(ChatRoomType.TRADE, "trade", "Trade Chat", null, null); }

    @Transactional
    public ChatDtos.RoomResponse game(String gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));
        return createOrJoin(ChatRoomType.GAME, "game:" + gameId, game.getTitle() + " Chat", game, null);
    }

    @Transactional
    public ChatDtos.RoomResponse group(UUID groupId) {
        User user = currentUser();
        GameGroup group = groupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("Group not found: " + groupId));
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) throw new AccessDeniedException("You must be a group member to enter its chat");
        return createOrJoin(ChatRoomType.GROUP, "group:" + groupId, group.getName() + " Chat", null, group);
    }

    @Transactional
    public ChatDtos.RoomResponse direct(Long otherUserId) {
        User user = currentUser();
        if (user.getId().equals(otherUserId)) throw new ConflictException("You cannot start a direct chat with yourself");
        User other = userRepository.findById(otherUserId).orElseThrow(() -> new ResourceNotFoundException("User not found: " + otherUserId));
        long first = Math.min(user.getId(), other.getId()), second = Math.max(user.getId(), other.getId());
        String key = "direct:" + first + ":" + second;
        ChatRoom room = roomRepository.findByRoomKey(key).orElseGet(() -> {
            ChatRoom created = roomRepository.save(new ChatRoom(UUID.randomUUID(), ChatRoomType.DIRECT, key,
                    other.getDisplayName() != null ? other.getDisplayName() : other.getUsername(), null, null, user));
            memberRepository.save(new ChatRoomMember(created, user));
            memberRepository.save(new ChatRoomMember(created, other));
            return created;
        });
        ensureMember(room, user);
        return toRoomResponse(room);
    }

    @Transactional
    public ChatDtos.RoomResponse join(UUID roomId) {
        User user = currentUser(); ChatRoom room = room(roomId);
        ensureCanAccess(room, user); return toRoomResponse(room);
    }

    @Transactional
    public void leave(UUID roomId) {
        User user = currentUser(); ChatRoom room = room(roomId);
        if (room.getRoomType() == ChatRoomType.GLOBAL || room.getRoomType() == ChatRoomType.TRADE) return;
        if (room.getRoomType() == ChatRoomType.DIRECT) {
            memberRepository.findByRoomIdAndUserId(roomId, user.getId()).ifPresent(memberRepository::delete);
            return;
        }
        memberRepository.findByRoomIdAndUserId(roomId, user.getId()).ifPresent(memberRepository::delete);
    }

    @Transactional public Page<ChatDtos.RoomResponse> myRooms(Pageable pageable) {
        User user = currentUser(); return roomRepository.findRoomsForUser(user.getId(), pageable).map(this::toRoomResponse);
    }

    @Transactional public Page<ChatDtos.MessageResponse> messages(UUID roomId, Pageable pageable) {
        User user = currentUser(); ChatRoom room = room(roomId); ensureCanAccess(room, user);
        return messageRepository.findByRoomIdAndDeletedAtIsNull(roomId, pageable).map(this::toMessageResponse);
    }

    @Transactional public ChatDtos.MessageResponse send(UUID roomId, ChatDtos.SendMessageRequest request) {
        User user = currentUser(); ChatRoom room = room(roomId); ensureCanAccess(room, user);
        ChatMessage message = messageRepository.save(new ChatMessage(UUID.randomUUID(), room, user, request.content().trim()));
        return toMessageResponse(message);
    }

    @Transactional public ChatDtos.MessageResponse edit(UUID messageId, ChatDtos.EditMessageRequest request) {
        User user = currentUser(); ChatMessage message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        ensureCanAccess(message.getRoom(), user);
        if (!message.getSender().getId().equals(user.getId()) && !isAdmin()) throw new AccessDeniedException("You can only edit your own messages");
        message.setContent(request.content().trim()); message.setEditedAt(OffsetDateTime.now());
        return toMessageResponse(messageRepository.save(message));
    }

    @Transactional public void delete(UUID messageId) {
        User user = currentUser(); ChatMessage message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        ensureCanAccess(message.getRoom(), user);
        if (!message.getSender().getId().equals(user.getId()) && !isAdmin()) throw new AccessDeniedException("You can only delete your own messages");
        message.setDeletedAt(OffsetDateTime.now()); messageRepository.save(message);
    }

    @Transactional public void markRead(UUID roomId) {
        User user = currentUser(); ChatRoom room = room(roomId); ensureCanAccess(room, user);
        ChatRoomMember member = memberRepository.findByRoomIdAndUserId(roomId, user.getId()).orElseThrow(() -> new AccessDeniedException("You are not a member of this chat"));
        member.setLastReadAt(OffsetDateTime.now()); memberRepository.save(member);
    }

    public void assertCanAccess(UUID roomId, User user) {
        ChatRoom room = room(roomId); ensureCanAccess(room, user);
    }

    private ChatDtos.RoomResponse toRoomResponse(ChatRoom room) {
        String gameId = room.getGame() == null ? null : room.getGame().getId();
        UUID groupId = room.getGroup() == null ? null : room.getGroup().getId();
        return new ChatDtos.RoomResponse(room.getId(), room.getRoomType(), room.getRoomKey(), room.getName(), gameId, groupId,
                memberRepository.countByRoomId(room.getId()), room.getCreatedAt());
    }

    private ChatDtos.MessageResponse toMessageResponse(ChatMessage message) {
        User sender = message.getSender();
        return new ChatDtos.MessageResponse(message.getId(), message.getRoom().getId(), sender.getId(), sender.getUsername(),
                sender.getDisplayName(), message.getContent(), message.getCreatedAt(), message.getEditedAt());
    }

    private ChatRoom createOrJoin(ChatRoomType type, String key, String name, Game game, GameGroup group) {
        User user = currentUser();
        ChatRoom room = roomRepository.findByRoomKey(key).orElseGet(() -> roomRepository.save(new ChatRoom(UUID.randomUUID(), type, key, name, game, group, user)));
        ensureMember(room, user); return room;
    }

    private void ensureMember(ChatRoom room, User user) {
        if (!memberRepository.existsByRoomIdAndUserId(room.getId(), user.getId())) memberRepository.save(new ChatRoomMember(room, user));
    }

    private void ensureCanAccess(ChatRoom room, User user) {
        if (room.getRoomType() == ChatRoomType.GROUP && room.getGroup() != null && !groupMemberRepository.existsByGroupIdAndUserId(room.getGroup().getId(), user.getId()))
            throw new AccessDeniedException("You must be a group member to use this chat");
        if (room.getRoomType() == ChatRoomType.DIRECT && !memberRepository.existsByRoomIdAndUserId(room.getId(), user.getId()))
            throw new AccessDeniedException("This direct chat is private");
        ensureMember(room, user);
    }

    private ChatRoom room(UUID id) { return roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Chat room not found: " + id)); }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) throw new AccessDeniedException("Authentication required");
        return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
