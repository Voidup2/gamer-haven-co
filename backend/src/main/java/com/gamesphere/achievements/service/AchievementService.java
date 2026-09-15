package com.gamesphere.achievements.service;

import com.gamesphere.achievements.api.*;
import com.gamesphere.achievements.domain.*;
import com.gamesphere.achievements.repository.*;
import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.notifications.domain.Notification.NotificationType;
import com.gamesphere.notifications.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AchievementService {
    private final GameAchievementRepository achievementRepository;
    private final UserGameAchievementRepository userAchievementRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserActivityService activityService;

    public AchievementService(GameAchievementRepository achievementRepository,
                              UserGameAchievementRepository userAchievementRepository,
                              GameRepository gameRepository,
                              UserRepository userRepository,
                              NotificationService notificationService,
                              UserActivityService activityService) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityService = activityService;
    }

    @Transactional
    public AchievementResponse create(String gameId, AchievementRequest request) {
        requireAdmin();
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        if (achievementRepository.existsByGameIdAndName(gameId, request.name())) {
            throw new ConflictException("Achievement already exists for this game");
        }
        return AchievementResponse.from(achievementRepository.save(
                new GameAchievement(game, request.name(), request.description(), request.points())));
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> list(String gameId) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        return achievementRepository.findByGameIdOrderByPointsAscNameAsc(gameId)
                .stream()
                .map(AchievementResponse::from)
                .toList();
    }

    @Transactional
    public AchievementResponse update(UUID id, AchievementRequest request) {
        requireAdmin();
        GameAchievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));
        if (achievementRepository.existsByGameIdAndNameAndIdNot(
                achievement.getGame().getId(), request.name(), id)) {
            throw new ConflictException("Achievement already exists for this game");
        }
        achievement.update(request.name(), request.description(), request.points());
        return AchievementResponse.from(achievementRepository.save(achievement));
    }

    @Transactional
    public void delete(UUID id) {
        requireAdmin();
        achievementRepository.delete(achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found")));
    }

    @Transactional
    public UserAchievementResponse updateProgress(UUID achievementId, AchievementProgressRequest request) {
        User user = currentUser();
        GameAchievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));
        UserGameAchievement userAchievement = userAchievementRepository
                .findByAchievementIdAndUserId(achievementId, user.getId())
                .orElse(null);

        boolean newlyUnlocked = userAchievement == null && request.progressPercent() == 100;
        if (userAchievement == null) {
            userAchievement = new UserGameAchievement(achievement, user, request.progressPercent());
        } else {
            newlyUnlocked = userAchievement.getUnlockedAt() == null
                    && request.progressPercent() == 100;
            userAchievement.updateProgress(request.progressPercent());
        }

        userAchievement = userAchievementRepository.save(userAchievement);
        if (newlyUnlocked) {
            notificationService.create(user, NotificationType.SYSTEM,
                    "Achievement unlocked", "You unlocked: " + achievement.getName(),
                    "GAME_ACHIEVEMENT", achievementId.toString());
            activityService.record(user, UserActivity.ActivityType.ACHIEVEMENT_UNLOCKED,
                    "Unlocked achievement", achievement.getName(),
                    "GAME_ACHIEVEMENT", achievementId.toString());
        }
        return UserAchievementResponse.from(userAchievement);
    }

    @Transactional(readOnly = true)
    public List<UserAchievementResponse> mine(String gameId) {
        User user = currentUser();
        if (gameId == null) {
            return userAchievementRepository.findByUserIdOrderByUnlockedAtDesc(user.getId())
                    .stream()
                    .map(UserAchievementResponse::from)
                    .toList();
        }

        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        return userAchievementRepository.findByAchievementGameIdAndUserId(gameId, user.getId())
                .stream()
                .map(UserAchievementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AchievementStatsResponse stats(String gameId) {
        User user = currentUser();
        gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        long total = achievementRepository.findByGameIdOrderByPointsAscNameAsc(gameId).size();
        long unlocked = userAchievementRepository
                .countByAchievementGameIdAndUserIdAndUnlockedAtIsNotNull(gameId, user.getId());
        int percent = total == 0 ? 0 : (int) Math.round(unlocked * 100.0 / total);
        int points = userAchievementRepository.findByAchievementGameIdAndUserId(gameId, user.getId())
                .stream()
                .filter(achievement -> achievement.getUnlockedAt() != null)
                .mapToInt(achievement -> achievement.getAchievement().getPoints())
                .sum();

        return new AchievementStatsResponse(gameId, total, unlocked, percent, points);
    }

    private void requireAdmin() {
        if (!isAdmin(currentUser())) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()));
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
