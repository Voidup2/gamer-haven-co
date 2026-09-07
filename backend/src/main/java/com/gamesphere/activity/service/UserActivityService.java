package com.gamesphere.activity.service;

import com.gamesphere.activity.api.UserActivityResponse;
import com.gamesphere.activity.api.UserActivitySummaryResponse;
import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.repository.UserActivityRepository;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class UserActivityService {
    private final UserActivityRepository repository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public UserActivityService(UserActivityRepository repository, UserRepository userRepository,
                               GameRepository gameRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
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

        List<OffsetDateTime> timestamps = repository.findCreatedAtByUserIdOrderByCreatedAtDesc(userId);
        Streaks streaks = calculateStreaks(timestamps);

        String mostActiveGameId = null;
        String mostActiveGameTitle = null;
        long mostActiveGameActivityCount = 0;
        List<Object[]> gameCounts = repository.findGameActivityCounts(userId, PageRequest.of(0, 1));
        if (!gameCounts.isEmpty()) {
            Object[] row = gameCounts.get(0);
            mostActiveGameId = (String) row[0];
            mostActiveGameActivityCount = ((Number) row[1]).longValue();
            mostActiveGameTitle = gameRepository.findById(mostActiveGameId)
                    .map(Game::getTitle)
                    .orElse(null);
        }

        return new UserActivitySummaryResponse(
                repository.countByUserId(userId),
                counts,
                timestamps.isEmpty() ? null : timestamps.get(0),
                progressUpdates,
                gamesCompleted,
                achievementsUnlocked,
                marketplacePurchases,
                marketplaceSales,
                streaks.activeDays,
                streaks.currentStreakDays,
                streaks.longestStreakDays,
                streaks.lastActiveDate,
                mostActiveGameId,
                mostActiveGameTitle,
                mostActiveGameActivityCount
        );
    }

    private static Streaks calculateStreaks(List<OffsetDateTime> timestamps) {
        if (timestamps.isEmpty()) {
            return new Streaks(0, 0, 0, null);
        }

        List<LocalDate> dates = timestamps.stream()
                .map(OffsetDateTime::toLocalDate)
                .distinct()
                .toList();

        long longest = 1;
        long streak = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i - 1).minusDays(1).equals(dates.get(i))) {
                streak++;
                longest = Math.max(longest, streak);
            } else {
                streak = 1;
            }
        }

        LocalDate today = LocalDate.now();
        long current = dates.get(0).equals(today) || dates.get(0).equals(today.minusDays(1)) ? 1 : 0;
        for (int i = 1; current > 0 && i < dates.size(); i++) {
            if (dates.get(i - 1).minusDays(1).equals(dates.get(i))) {
                current++;
            } else {
                break;
            }
        }

        return new Streaks(dates.size(), current, longest, dates.get(0));
    }

    private record Streaks(long activeDays, long currentStreakDays, long longestStreakDays,
                           LocalDate lastActiveDate) {}

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(a.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
