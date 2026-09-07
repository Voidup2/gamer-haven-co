package com.gamesphere.notifications.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.notifications.api.NotificationResponse;
import com.gamesphere.notifications.domain.Notification;
import com.gamesphere.notifications.domain.Notification.NotificationType;
import com.gamesphere.notifications.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationResponse create(User user, NotificationType type, String title,
                                       String message, String referenceType, String referenceId) {
        return NotificationResponse.from(notificationRepository.save(
                new Notification(user, type, title, message, referenceType, referenceId)));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> findMine(Pageable pageable) {
        Long userId = getCurrentUser().getId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(getCurrentUser().getId());
    }

    @Transactional
    public NotificationResponse markRead(UUID id) {
        Notification notification = findOwned(id);
        notification.markRead();
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead() {
        User user = getCurrentUser();
        notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), Pageable.unpaged())
                .forEach(notification -> {
                    if (!notification.isRead()) {
                        notification.markRead();
                        notificationRepository.save(notification);
                    }
                });
    }

    @Transactional
    public void deleteMine() {
        notificationRepository.deleteByUserId(getCurrentUser().getId());
    }

    private Notification findOwned(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("You are not allowed to access this notification");
        }
        return notification;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
