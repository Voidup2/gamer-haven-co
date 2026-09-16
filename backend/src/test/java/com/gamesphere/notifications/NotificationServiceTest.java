package com.gamesphere.notifications;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.notifications.api.NotificationResponse;
import com.gamesphere.notifications.domain.Notification;
import com.gamesphere.notifications.repository.NotificationRepository;
import com.gamesphere.notifications.service.NotificationPreferencesService;
import com.gamesphere.notifications.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationPreferencesService preferencesService;
    @Mock private User user;
    @Mock private User owner;
    @Mock private Notification notification;

    @InjectMocks private NotificationService service;

    private UUID notificationId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        notificationId = UUID.randomUUID();

        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getUsername()).thenReturn("alice");
        lenient().when(owner.getId()).thenReturn(1L);
        lenient().when(notification.getId()).thenReturn(notificationId);
        lenient().when(notification.getType()).thenReturn(Notification.NotificationType.SYSTEM);
        lenient().when(notification.getTitle()).thenReturn("System notice");
        lenient().when(notification.getMessage()).thenReturn("Hello");
        lenient().when(notification.getReferenceType()).thenReturn("SYSTEM");
        lenient().when(notification.getReferenceId()).thenReturn("ref-1");
        lenient().when(notification.getCreatedAt()).thenReturn(OffsetDateTime.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSavesNotificationWhenAllowed() {
        when(preferencesService.allows(user, Notification.NotificationType.SYSTEM)).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = service.create(
                user, Notification.NotificationType.SYSTEM, "System notice", "Hello", "SYSTEM", "ref-1");

        assertNotNull(response);
        assertEquals(notificationId, response.id());
        assertEquals(Notification.NotificationType.SYSTEM, response.type());
        assertEquals("System notice", response.title());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createReturnsNullWhenPreferenceDisallows() {
        when(preferencesService.allows(user, Notification.NotificationType.MARKETPLACE)).thenReturn(false);

        NotificationResponse response = service.create(
                user, Notification.NotificationType.MARKETPLACE, "Sale", "Message", "LISTING", "1");

        assertNull(response);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void findMineReturnsMappedPage() {
        authenticate();
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(page);

        Page<NotificationResponse> result = service.findMine(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(notificationId, result.getContent().get(0).id());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    void unreadCountReturnsRepositoryCount() {
        authenticate();
        when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(4L);

        assertEquals(4L, service.unreadCount());
    }

    @Test
    void markReadMarksOwnedNotification() {
        authenticate();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notification.getUser()).thenReturn(owner);
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = service.markRead(notificationId);

        verify(notification).markRead();
        verify(notificationRepository).save(notification);
        assertEquals(notificationId, response.id());
    }

    @Test
    void markReadRejectsUnknownNotification() {
        authenticate();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.markRead(notificationId));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markReadRejectsAnotherUsersNotification() {
        authenticate();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notification.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(2L);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.markRead(notificationId));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllReadMarksOnlyUnreadNotifications() {
        authenticate();
        Notification unread = mock(Notification.class);
        Notification read = mock(Notification.class);
        when(unread.isRead()).thenReturn(false);
        when(read.isRead()).thenReturn(true);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(unread, read)));

        service.markAllRead();

        verify(unread).markRead();
        verify(notificationRepository).save(unread);
        verify(read, never()).markRead();
        verify(notificationRepository, never()).save(read);
    }

    @Test
    void deleteMineDeletesCurrentUsersNotifications() {
        authenticate();

        service.deleteMine();

        verify(notificationRepository).deleteByUserId(1L);
    }

    @Test
    void unauthenticatedUserIsRejected() {
        SecurityContextHolder.clearContext();

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.unreadCount());
    }

    private void authenticate() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of()));
    }
}
