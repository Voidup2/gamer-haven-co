package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ChatUnreadService {
    private final ChatRoomMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatUnreadService(ChatRoomMemberRepository memberRepository, ChatMessageRepository messageRepository, UserRepository userRepository) {
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatDtos.UnreadRoomResponse> rooms() {
        User user = currentUser();
        return memberRepository.findByUserId(user.getId()).stream()
                .map(member -> new ChatDtos.UnreadRoomResponse(member.getRoom().getId(), unread(member.getLastReadAt(), member.getJoinedAt(), member.getRoom().getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatDtos.UnreadTotalResponse total() {
        return new ChatDtos.UnreadTotalResponse(rooms().stream().mapToLong(ChatDtos.UnreadRoomResponse::unreadCount).sum());
    }

    @Transactional(readOnly = true)
    public long count(UUID roomId) {
        User user = currentUser();
        ChatRoomMember member = memberRepository.findByRoomIdAndUserId(roomId, user.getId()).orElse(null);
        if (member == null) return 0;
        return unread(member.getLastReadAt(), member.getJoinedAt(), roomId);
    }

    private long unread(OffsetDateTime lastReadAt, OffsetDateTime joinedAt, java.util.UUID roomId) {
        return messageRepository.countByRoomIdAndCreatedAtAfter(roomId, lastReadAt != null ? lastReadAt : joinedAt);
    }

    private User currentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Authenticated user not found"));
    }
}
