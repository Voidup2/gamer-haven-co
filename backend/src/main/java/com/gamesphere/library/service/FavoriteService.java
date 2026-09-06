package com.gamesphere.library.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.domain.UserGameFavorite;
import com.gamesphere.library.repository.UserGameFavoriteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final UserGameFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public FavoriteService(UserGameFavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           GameRepository gameRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public UserGameFavorite addGame(String gameId) {
        User user = getCurrentUser();
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        if (favoriteRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new ConflictException("Game is already in your favorites");
        }

        return favoriteRepository.save(new UserGameFavorite(user, game));
    }

    @Transactional(readOnly = true)
    public List<UserGameFavorite> getFavorites() {
        return favoriteRepository.findByUserIdOrderByAddedAtDesc(getCurrentUser().getId());
    }

    @Transactional
    public void removeGame(String gameId) {
        User user = getCurrentUser();
        if (!favoriteRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new ResourceNotFoundException("Game is not in your favorites");
        }
        favoriteRepository.deleteByUserIdAndGameId(user.getId(), gameId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String gameId) {
        return favoriteRepository.existsByUserIdAndGameId(getCurrentUser().getId(), gameId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
