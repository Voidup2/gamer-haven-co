package com.gamesphere.activity.service;

import com.gamesphere.activity.api.UserActivityResponse;
import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.repository.UserActivityRepository;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActivityService {
    private final UserActivityRepository repository;
    private final UserRepository userRepository;

    public UserActivityService(UserActivityRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserActivityResponse record(User user, UserActivity.ActivityType type, String title,
                                       String description, String referenceType, String referenceId) {
        return UserActivityResponse.from(repository.save(
                new UserActivity(user, type, title, description, referenceType, referenceId)));
    }

    @Transactional(readOnly = true)
    public Page<UserActivityResponse> mine(UserActivity.ActivityType type, Pageable pageable) {
        Long userId = currentUser().getId();
        Page<UserActivity> page = type == null
                ? repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : repository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(userId, type, pageable);
        return page.map(UserActivityResponse::from);
    }

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(a.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
