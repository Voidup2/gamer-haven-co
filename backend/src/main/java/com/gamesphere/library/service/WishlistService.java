package com.gamesphere.library.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.domain.UserGameWishlist;
import com.gamesphere.library.repository.UserGameWishlistRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final UserGameWishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public WishlistService(
            UserGameWishlistRepository wishlistRepository,
            UserRepository userRepository,
            GameRepository gameRepository
    ) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public UserGameWishlist addGame(String gameId) {

        User user = getCurrentUser();

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Game not found")
                );

        boolean alreadyExists =
                wishlistRepository.existsByUserIdAndGameId(
                        user.getId(),
                        gameId
                );

        if (alreadyExists) {
            throw new ConflictException(
                    "Game is already in your wishlist"
            );
        }

        UserGameWishlist wishlistEntry =
                new UserGameWishlist(user, game);

        return wishlistRepository.save(wishlistEntry);
    }

    @Transactional(readOnly = true)
    public List<UserGameWishlist> getWishlist() {

        User user = getCurrentUser();

        return wishlistRepository.findByUserId(user.getId());
    }

    @Transactional
    public void removeGame(String gameId) {

        User user = getCurrentUser();

        boolean exists =
                wishlistRepository.existsByUserIdAndGameId(
                        user.getId(),
                        gameId
                );

        if (!exists) {
            throw new ResourceNotFoundException(
                    "Game is not in your wishlist"
            );
        }

        wishlistRepository.deleteByUserIdAndGameId(
                user.getId(),
                gameId
        );
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }
}