package com.gamesphere.recommendations.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.domain.UserGameLibrary;
import com.gamesphere.library.domain.UserGameWishlist;
import com.gamesphere.library.repository.UserGameLibraryRepository;
import com.gamesphere.library.repository.UserGameWishlistRepository;
import com.gamesphere.recommendations.api.RecommendationResponse;
import com.gamesphere.reviews.domain.Review;
import com.gamesphere.reviews.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final UserGameLibraryRepository libraryRepository;
    private final UserGameWishlistRepository wishlistRepository;
    private final ReviewRepository reviewRepository;

    public RecommendationService(GameRepository gameRepository,
                                  UserRepository userRepository,
                                  UserGameLibraryRepository libraryRepository,
                                  UserGameWishlistRepository wishlistRepository,
                                  ReviewRepository reviewRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.wishlistRepository = wishlistRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Page<RecommendationResponse> recommend(Pageable pageable) {
        User user = getCurrentUser();

        Set<String> ownedIds = libraryRepository.findByUserId(user.getId()).stream()
                .map(item -> item.getGame().getId())
                .collect(Collectors.toSet());
        Set<String> wishlistIds = wishlistRepository.findByUserId(user.getId()).stream()
                .map(item -> item.getGame().getId())
                .collect(Collectors.toSet());

        List<Review> reviews = reviewRepository.findByUserId(user.getId());

        Map<String, Double> genreWeights = new HashMap<>();
        Map<String, Double> platformWeights = new HashMap<>();
        for (Review review : reviews) {
            double weight = Math.max(0.5, review.getRating().doubleValue() / 10.0);
            review.getGame().getGenres().forEach(g -> genreWeights.merge(g.toLowerCase(), weight, Double::sum));
            review.getGame().getPlatforms().forEach(p -> platformWeights.merge(p.toLowerCase(), weight, Double::sum));
        }

        for (String id : wishlistIds) {
            gameRepository.findById(id).ifPresent(game -> {
                game.getGenres().forEach(g -> genreWeights.merge(g.toLowerCase(), 1.5, Double::sum));
                game.getPlatforms().forEach(p -> platformWeights.merge(p.toLowerCase(), 1.5, Double::sum));
            });
        }

        List<RecommendationResponse> recommendations = gameRepository.findAll().stream()
                .filter(game -> !ownedIds.contains(game.getId()))
                .map(game -> score(game, genreWeights, platformWeights, wishlistIds.contains(game.getId())))
                .sorted(Comparator.comparing(RecommendationResponse::score).reversed()
                        .thenComparing(r -> r.game().title(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= recommendations.size()) {
            return new PageImpl<>(List.of(), pageable, recommendations.size());
        }
        int end = Math.min(start + pageable.getPageSize(), recommendations.size());
        return new PageImpl<>(recommendations.subList(start, end), pageable, recommendations.size());
    }

    private RecommendationResponse score(Game game, Map<String, Double> genreWeights,
                                         Map<String, Double> platformWeights, boolean wishlisted) {
        double score = game.getRating() == null ? 0.0 : game.getRating().doubleValue() * 0.5;
        double genreScore = game.getGenres().stream()
                .mapToDouble(g -> genreWeights.getOrDefault(g.toLowerCase(), 0.0))
                .sum();
        double platformScore = game.getPlatforms().stream()
                .mapToDouble(p -> platformWeights.getOrDefault(p.toLowerCase(), 0.0))
                .sum();

        score += Math.min(5.0, genreScore);
        score += Math.min(2.0, platformScore);
        if (wishlisted) {
            score += 2.0;
        }

        String reason;
        if (wishlisted) {
            reason = "From your wishlist";
        } else if (genreScore > 0 && platformScore > 0) {
            reason = "Matches your rated genres and platforms";
        } else if (genreScore > 0) {
            reason = "Matches genres you have rated";
        } else if (platformScore > 0) {
            reason = "Matches platforms you have rated";
        } else {
            reason = "Highly rated by the community";
        }

        return new RecommendationResponse(
                GameResponse.from(game),
                BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP),
                reason
        );
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
