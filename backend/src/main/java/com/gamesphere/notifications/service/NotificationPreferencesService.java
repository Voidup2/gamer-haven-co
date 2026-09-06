package com.gamesphere.notifications.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.notifications.api.NotificationPreferencesRequest;
import com.gamesphere.notifications.api.NotificationPreferencesResponse;
import com.gamesphere.notifications.domain.NotificationPreferences;
import com.gamesphere.notifications.repository.NotificationPreferencesRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationPreferencesService {
    private final NotificationPreferencesRepository repository;
    private final UserRepository userRepository;

    public NotificationPreferencesService(NotificationPreferencesRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationPreferencesResponse getMine() {
        User user = currentUser();
        NotificationPreferences preferences = repository.findById(user.getId())
                .orElseGet(() -> repository.save(new NotificationPreferences(user)));
        return NotificationPreferencesResponse.from(preferences);
    }

    @Transactional
    public NotificationPreferencesResponse update(NotificationPreferencesRequest request) {
        User user = currentUser();
        NotificationPreferences preferences = repository.findById(user.getId())
                .orElseGet(() -> new NotificationPreferences(user));
        preferences.update(request.marketplaceEnabled(), request.wishlistEnabled(),
                request.upcomingReleaseEnabled(), request.replyEnabled(), request.mentionEnabled());
        return NotificationPreferencesResponse.from(repository.save(preferences));
    }

    public boolean allows(User user, com.gamesphere.notifications.domain.Notification.NotificationType type) {
        return repository.findById(user.getId()).map(p -> p.allows(type)).orElse(true);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
