package com.gamesphere.notifications;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.notifications.api.NotificationPreferencesRequest;
import com.gamesphere.notifications.api.NotificationPreferencesResponse;
import com.gamesphere.notifications.domain.Notification;
import com.gamesphere.notifications.domain.NotificationPreferences;
import com.gamesphere.notifications.repository.NotificationPreferencesRepository;
import com.gamesphere.notifications.service.NotificationPreferencesService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceTest {

    @Mock private NotificationPreferencesRepository repository;
    @Mock private UserRepository userRepository;
    @Mock private User user;
    @Mock private NotificationPreferences preferences;

    @InjectMocks private NotificationPreferencesService service;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        lenient().when(user.getId()).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMineReturnsExistingPreferences() {
        authenticate();
        stubAllEnabled();
        when(repository.findById(1L)).thenReturn(Optional.of(preferences));

        NotificationPreferencesResponse response = service.getMine();

        assertTrue(response.marketplaceEnabled());
        assertTrue(response.wishlistEnabled());
        assertTrue(response.upcomingReleaseEnabled());
        assertTrue(response.replyEnabled());
        assertTrue(response.mentionEnabled());
        verify(repository, never()).save(any(NotificationPreferences.class));
    }

    @Test
    void getMineCreatesDefaultPreferencesWhenMissing() {
        authenticate();
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(any(NotificationPreferences.class))).thenReturn(preferences);
        stubAllEnabled();

        NotificationPreferencesResponse response = service.getMine();

        assertTrue(response.marketplaceEnabled());
        assertTrue(response.wishlistEnabled());
        assertTrue(response.upcomingReleaseEnabled());
        assertTrue(response.replyEnabled());
        assertTrue(response.mentionEnabled());
        verify(repository).save(any(NotificationPreferences.class));
    }

    @Test
    void updateChangesExistingPreferences() {
        authenticate();
        when(repository.findById(1L)).thenReturn(Optional.of(preferences));
        when(repository.save(preferences)).thenReturn(preferences);
        stubDisabled();

        NotificationPreferencesRequest request =
                new NotificationPreferencesRequest(false, true, false, true, false);

        NotificationPreferencesResponse response = service.update(request);

        verify(preferences).update(false, true, false, true, false);
        verify(repository).save(preferences);
        assertFalse(response.marketplaceEnabled());
        assertFalse(response.upcomingReleaseEnabled());
        assertFalse(response.mentionEnabled());
    }

    @Test
    void updateCreatesPreferencesWhenMissing() {
        authenticate();
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(any(NotificationPreferences.class))).thenReturn(preferences);
        stubDisabled();

        NotificationPreferencesRequest request =
                new NotificationPreferencesRequest(false, false, true, false, true);

        NotificationPreferencesResponse response = service.update(request);

        assertNotNull(response);
        verify(repository).save(any(NotificationPreferences.class));
        verify(preferences).update(false, false, true, false, true);
    }

    @Test
    void allowsReturnsTrueWhenPreferencesDoNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertTrue(service.allows(user, Notification.NotificationType.MARKETPLACE));
    }

    @Test
    void allowsDelegatesToStoredPreferences() {
        when(repository.findById(1L)).thenReturn(Optional.of(preferences));
        when(preferences.allows(Notification.NotificationType.MARKETPLACE)).thenReturn(false);

        assertFalse(service.allows(user, Notification.NotificationType.MARKETPLACE));
        verify(preferences).allows(Notification.NotificationType.MARKETPLACE);
    }

    @Test
    void allowsSystemNotificationsWhenStoredPreferencesDenyOtherTypes() {
        when(repository.findById(1L)).thenReturn(Optional.of(preferences));
        when(preferences.allows(Notification.NotificationType.SYSTEM)).thenReturn(true);

        assertTrue(service.allows(user, Notification.NotificationType.SYSTEM));
    }

    @Test
    void unauthenticatedGetMineIsRejected() {
        SecurityContextHolder.clearContext();

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.getMine());
    }

    private void authenticate() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of()));
    }

    private void stubAllEnabled() {
        when(preferences.isMarketplaceEnabled()).thenReturn(true);
        when(preferences.isWishlistEnabled()).thenReturn(true);
        when(preferences.isUpcomingReleaseEnabled()).thenReturn(true);
        when(preferences.isReplyEnabled()).thenReturn(true);
        when(preferences.isMentionEnabled()).thenReturn(true);
    }

    private void stubDisabled() {
        when(preferences.isMarketplaceEnabled()).thenReturn(false);
        when(preferences.isWishlistEnabled()).thenReturn(false);
        when(preferences.isUpcomingReleaseEnabled()).thenReturn(false);
        when(preferences.isReplyEnabled()).thenReturn(false);
        when(preferences.isMentionEnabled()).thenReturn(false);
    }
}
