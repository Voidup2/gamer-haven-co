package com.gamesphere.achievements;

import com.gamesphere.achievements.api.AchievementRequest;
import com.gamesphere.achievements.domain.GameAchievement;
import com.gamesphere.achievements.repository.GameAchievementRepository;
import com.gamesphere.achievements.repository.UserGameAchievementRepository;
import com.gamesphere.achievements.service.AchievementService;
import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.notifications.service.NotificationService;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {
    @Mock GameAchievementRepository achievementRepository;
    @Mock UserGameAchievementRepository userAchievementRepository;
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @Mock UserActivityService activityService;
    @Mock Authentication authentication;
    @Mock User user;
    @Mock Game game;
    @Mock Role adminRole;

    @InjectMocks AchievementService achievementService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRejectsDuplicateAchievementForGame() {
        authenticateAsAdmin();
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(game));
        when(achievementRepository.existsByGameIdAndName("game-1", "First Win")).thenReturn(true);

        assertThrows(ConflictException.class, () -> achievementService.create(
                "game-1", new AchievementRequest("First Win", "Win once", 10)));
        verify(achievementRepository, never()).save(any(GameAchievement.class));
    }

    @Test
    void createRejectsUnknownGame() {
        authenticateAsAdmin();
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> achievementService.create(
                "missing-game", new AchievementRequest("First Win", "Win once", 10)));
        verify(achievementRepository, never()).existsByGameIdAndName(anyString(), anyString());
        verify(achievementRepository, never()).save(any(GameAchievement.class));
    }

    @Test
    void mineRejectsUnknownGameFilter() {
        authenticateAsUser();
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> achievementService.mine("missing-game"));
        verify(userAchievementRepository, never()).findByAchievementGameIdAndUserId(anyString(), anyLong());
    }

    @Test
    void statsRejectsUnknownGame() {
        authenticateAsUser();
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> achievementService.stats("missing-game"));
        verify(achievementRepository, never()).findByGameIdOrderByPointsAscNameAsc(anyString());
        verify(userAchievementRepository, never())
                .countByAchievementGameIdAndUserIdAndUnlockedAtIsNotNull(anyString(), anyLong());
    }

    private void authenticateAsAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(user.getRoles()).thenReturn(new HashSet<>(Set.of(adminRole)));
        when(adminRole.getName()).thenReturn("ADMIN");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateAsUser() {
        when(userRepository.findByUsername("player")).thenReturn(Optional.of(user));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("player");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
