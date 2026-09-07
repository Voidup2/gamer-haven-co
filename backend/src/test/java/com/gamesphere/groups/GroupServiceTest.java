package com.gamesphere.groups;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.groups.domain.GameGroup;
import com.gamesphere.groups.domain.GroupMember;
import com.gamesphere.groups.repository.GameGroupRepository;
import com.gamesphere.groups.repository.GroupMemberRepository;
import com.gamesphere.groups.service.GroupService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {
    @Mock GameGroupRepository groupRepository;
    @Mock GroupMemberRepository memberRepository;
    @Mock UserRepository userRepository;
    @Mock Authentication authentication;
    @InjectMocks GroupService groupService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void privateGroupCannotBeJoined() {
        User user = mock(User.class);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        GameGroup group = mock(GameGroup.class);
        UUID id = UUID.randomUUID();
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));
        when(group.isPublicGroup()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> groupService.join(id));
        verify(memberRepository, never()).save(any(GroupMember.class));
    }
}
