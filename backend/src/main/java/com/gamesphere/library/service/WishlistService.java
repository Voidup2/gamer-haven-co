package com.gamesphere.library.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.domain.UserGameWishlist;
import com.gamesphere.library.dto.WishlistGameResponse;
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
    public WishlistGameResponse addGame(String gameId) {
        User user = getCurrentUser();

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Game not found")
                );

        if (wishlistRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new ConflictException("Game is already in your wishlist");
        }

        UserGameWishlist wishlistEntry =
                wishlistRepository.save(new UserGameWishlist(user, game));

        return toResponse(wishlistEntry);
    }

    @Transactional(readOnly = true)
    public List<WishlistGameResponse> getWishlist() {
        User user = getCurrentUser();

        return wishlistRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void removeGame(String gameId) {
        User user = getCurrentUser();

        if (!wishlistRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new ResourceNotFoundException("Game is not in your wishlist");
        }

        wishlistRepository.deleteByUserIdAndGameId(user.getId(), gameId);
    }

    private WishlistGameResponse toResponse(UserGameWishlist entry) {
        return new WishlistGameResponse(
                entry.getGame().getId(),
                entry.getGame().getTitle(),
                entry.getAddedAt()
        );
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user not found")
                );
    }
}
