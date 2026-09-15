package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.blocks.repository.UserBlockRepository;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.groups.repository.GameGroupRepository;
import com.gamesphere.groups.repository.GroupMemberRepository;
import com.gamesphere.notifications.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock ChatRoomRepository roomRepository;
    @Mock ChatRoomMemberRepository memberRepository;
    @Mock ChatMessageRepository messageRepository;
    @Mock UserRepository userRepository;
    @Mock UserBlockRepository blockRepository;
    @Mock GameRepository gameRepository;
    @Mock GameGroupRepository groupRepository;
    @Mock GroupMemberRepository groupMemberRepository;
    @Mock ChatRealtimePublisher realtimePublisher;
    @Mock NotificationService notificationService;
    @Mock Authentication authentication;
    @Mock User user;
    @Mock ChatRoom room;

    @InjectMocks ChatService chatService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void groupChatRejectsNonMember() {
        UUID groupId = UUID.randomUUID();
        authenticateAsUser(1L, "player");
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(mock(com.gamesphere.groups.domain.GameGroup.class)));
        when(groupMemberRepository.existsByGroupIdAndUserId(groupId, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> chatService.group(groupId));
        verify(roomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void directChatRejectsBlockedUser() {
        long otherUserId = 2L;
        User other = mock(User.class);
        authenticateAsUser(1L, "player");
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(other));
        when(other.getId()).thenReturn(otherUserId);
        when(blockRepository.existsByBlockerIdAndBlockedId(1L, otherUserId)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> chatService.direct(otherUserId));
        verify(roomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void directChatRejectsSelf() {
        authenticateAsUser(1L, "player");

        assertThrows(com.gamesphere.common.exception.ConflictException.class, () -> chatService.direct(1L));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void directChatRejectsUnknownUser() {
        authenticateAsUser(1L, "player");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(com.gamesphere.common.exception.ResourceNotFoundException.class, () -> chatService.direct(99L));
        verify(roomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    void privateDirectRoomRejectsNonMemberAccess() {
        UUID roomId = UUID.randomUUID();
        authenticateAsUser(1L, "player");
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.DIRECT);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> chatService.messages(roomId, org.springframework.data.domain.PageRequest.of(0, 20)));
        verify(messageRepository, never()).findByRoomIdAndDeletedAtIsNull(any(), any());
    }

    private void authenticateAsUser(long userId, String username) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
