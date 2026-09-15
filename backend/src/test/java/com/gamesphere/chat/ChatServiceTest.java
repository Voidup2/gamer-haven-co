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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void sendRejectsNonMemberOfDirectRoom() {
        UUID roomId = UUID.randomUUID();
        authenticateAsUser(1L, "player");
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.DIRECT);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> chatService.send(roomId, new ChatDtos.SendMessageRequest("hello")));
        verify(messageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void editRejectsOtherUsersMessage() {
        UUID roomId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        User sender = mock(User.class);
        ChatMessage message = mock(ChatMessage.class);
        authenticateAsUser(1L, "player");
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(message.getRoom()).thenReturn(room);
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.GLOBAL);
        when(sender.getId()).thenReturn(2L);
        when(message.getSender()).thenReturn(sender);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);
        when(memberRepository.save(any(ChatRoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(AccessDeniedException.class, () -> chatService.edit(messageId, new ChatDtos.EditMessageRequest("edited")));
        verify(messageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void editAllowsOwnMessage() {
        UUID roomId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        ChatMessage message = mock(ChatMessage.class);
        authenticateAsUser(1L, "player");
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(message.getRoom()).thenReturn(room);
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.GLOBAL);
        when(message.getSender()).thenReturn(user);
        when(message.getId()).thenReturn(messageId);
        when(message.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(message.getEditedAt()).thenReturn(null);
        when(message.getContent()).thenReturn("edited");
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("player");
        when(user.getDisplayName()).thenReturn("Player");
        when(messageRepository.save(message)).thenReturn(message);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);
        when(memberRepository.save(any(ChatRoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatService.edit(messageId, new ChatDtos.EditMessageRequest(" edited "));

        verify(message).setContent("edited");
        verify(message).setEditedAt(any(OffsetDateTime.class));
        verify(messageRepository).save(message);
        verify(realtimePublisher).messageEdited(any(ChatDtos.MessageResponse.class));
    }

    @Test
    void deleteRejectsOtherUsersMessage() {
        UUID roomId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        User sender = mock(User.class);
        ChatMessage message = mock(ChatMessage.class);
        authenticateAsUser(1L, "player");
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(message.getRoom()).thenReturn(room);
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.GLOBAL);
        when(sender.getId()).thenReturn(2L);
        when(message.getSender()).thenReturn(sender);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);
        when(memberRepository.save(any(ChatRoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(AccessDeniedException.class, () -> chatService.delete(messageId));
        verify(messageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void deleteAllowsOwnMessage() {
        UUID roomId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        ChatMessage message = mock(ChatMessage.class);
        authenticateAsUser(1L, "player");
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(message.getRoom()).thenReturn(room);
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.GLOBAL);
        when(message.getSender()).thenReturn(user);
        when(message.getId()).thenReturn(messageId);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("player");
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);
        when(memberRepository.save(any(ChatRoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(message)).thenReturn(message);

        chatService.delete(messageId);

        verify(message).setDeletedAt(any(OffsetDateTime.class));
        verify(messageRepository).save(message);
        verify(realtimePublisher).messageDeleted(roomId, messageId, 1L, "player");
    }

    @Test
    void adminCanEditAnotherUsersMessage() {
        UUID roomId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        User sender = mock(User.class);
        ChatMessage message = mock(ChatMessage.class);
        authenticateAsAdmin(1L, "admin");
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(message.getRoom()).thenReturn(room);
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.GLOBAL);
        when(sender.getId()).thenReturn(2L);
        when(message.getSender()).thenReturn(sender);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);
        when(memberRepository.save(any(ChatRoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(message)).thenReturn(message);
        when(message.getId()).thenReturn(messageId);
        when(message.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(message.getEditedAt()).thenReturn(null);
        when(message.getContent()).thenReturn("edited");
        when(sender.getUsername()).thenReturn("player");
        when(sender.getDisplayName()).thenReturn("Player");

        chatService.edit(messageId, new ChatDtos.EditMessageRequest("edited"));

        verify(message).setContent("edited");
        verify(messageRepository).save(message);
    }

    @Test
    void adminCanDeleteAnotherUsersMessage() {
        UUID roomId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        User sender = mock(User.class);
        ChatMessage message = mock(ChatMessage.class);
        authenticateAsAdmin(1L, "admin");
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(message.getRoom()).thenReturn(room);
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.GLOBAL);
        when(sender.getId()).thenReturn(2L);
        when(message.getSender()).thenReturn(sender);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(false);
        when(memberRepository.save(any(ChatRoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(message)).thenReturn(message);
        when(message.getId()).thenReturn(messageId);
        when(user.getUsername()).thenReturn("admin");

        chatService.delete(messageId);

        verify(message).setDeletedAt(any(OffsetDateTime.class));
        verify(messageRepository).save(message);
        verify(realtimePublisher).messageDeleted(roomId, messageId, 1L, "admin");
    }

    @Test
    void directMessageNotifiesRecipient() {
        UUID roomId = UUID.randomUUID();
        User recipient = mock(User.class);
        ChatMessage saved = mock(ChatMessage.class);
        authenticateAsUser(1L, "player");
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(room.getId()).thenReturn(roomId);
        when(room.getRoomType()).thenReturn(ChatRoomType.DIRECT);
        when(memberRepository.existsByRoomIdAndUserId(roomId, 1L)).thenReturn(true);
        when(memberRepository.findOtherUser(roomId, 1L)).thenReturn(Optional.of(recipient));
        when(recipient.getId()).thenReturn(2L);
        when(recipient.getDisplayName()).thenReturn("Other Player");
        when(recipient.getUsername()).thenReturn("other");
        when(blockRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(false);
        when(blockRepository.existsByBlockerIdAndBlockedId(2L, 1L)).thenReturn(false);
        when(messageRepository.save(any(ChatMessage.class))).thenReturn(saved);
        when(saved.getId()).thenReturn(UUID.randomUUID());
        when(saved.getRoom()).thenReturn(room);
        when(saved.getSender()).thenReturn(user);
        when(saved.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(saved.getEditedAt()).thenReturn(null);
        when(saved.getContent()).thenReturn("hello");
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("player");
        when(user.getDisplayName()).thenReturn("Player");

        chatService.send(roomId, new ChatDtos.SendMessageRequest(" hello "));

        verify(notificationService).create(eq(recipient), any(), eq("New direct message"),
                eq("Player sent you a message"), eq("CHAT_ROOM"), eq(roomId.toString()));
        verify(realtimePublisher).messageSent(any(ChatDtos.MessageResponse.class));
    }

    private void authenticateAsUser(long userId, String username) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateAsAdmin(long userId, String username) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(user);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
