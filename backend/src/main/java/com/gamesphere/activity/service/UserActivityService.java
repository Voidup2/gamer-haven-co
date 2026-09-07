package com.gamesphere.activity.service;

import com.gamesphere.activity.api.UserActivityResponse;
import com.gamesphere.activity.api.UserActivitySummaryResponse;
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

import java.util.EnumMap;
import java.util.Map;

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
    public UserActivity findLatest(User user, UserActivity.ActivityType type,
                                   String referenceType, String referenceId) {
        return repository.findFirstByUserIdAndActivityTypeAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
                        user.getId(), type, referenceType, referenceId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<UserActivityResponse> mine(UserActivity.ActivityType type, Pageable pageable) {
        Long userId = currentUser().getId();
        Page<UserActivity> page = type == null
                ? repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : repository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(userId, type, pageable);
        return page.map(UserActivityResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<UserActivityResponse> mineForGame(String gameId, Pageable pageable) {
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId must not be blank");
        }
        return repository.findByUserIdAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
                        currentUser().getId(), "GAME", gameId, pageable)
                .map(UserActivityResponse::from);
    }

    @Transactional(readOnly = true)
    public UserActivitySummaryResponse summary() {
        Long userId = currentUser().getId();
        Map<UserActivity.ActivityType, Long> counts = new EnumMap<>(UserActivity.ActivityType.class);
        for (UserActivity.ActivityType type : UserActivity.ActivityType.values()) {
            long count = repository.countByUserIdAndActivityType(userId, type);
            if (count > 0) {
                counts.put(type, count);
            }
        }

        long progressUpdates = counts.getOrDefault(UserActivity.ActivityType.PROGRESS_UPDATED, 0L);
        long gamesCompleted = counts.getOrDefault(UserActivity.ActivityType.GAME_COMPLETED, 0L);
        long achievementsUnlocked = counts.getOrDefault(UserActivity.ActivityType.ACHIEVEMENT_UNLOCKED, 0L);
        long marketplacePurchases = counts.getOrDefault(UserActivity.ActivityType.MARKETPLACE_PURCHASE, 0L);
        long marketplaceSales = counts.getOrDefault(UserActivity.ActivityType.MARKETPLACE_SALE, 0L);

        return new UserActivitySummaryResponse(
                repository.countByUserId(userId),
                counts,
                repository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                        .map(UserActivity::getCreatedAt)
                        .orElse(null),
                progressUpdates,
                gamesCompleted,
                achievementsUnlocked,
                marketplacePurchases,
                marketplaceSales
        );
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
