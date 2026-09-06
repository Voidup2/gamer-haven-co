package com.gamesphere.reviews.service;

import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.reviews.api.ReviewRequest;
import com.gamesphere.reviews.domain.Review;
import com.gamesphere.reviews.repository.ReviewRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository; private final UserRepository userRepository; private final GameRepository gameRepository; private final UserActivityService activityService;
    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, GameRepository gameRepository, UserActivityService activityService) { this.reviewRepository=reviewRepository; this.userRepository=userRepository; this.gameRepository=gameRepository; this.activityService=activityService; }
    @Transactional public Review createReview(String gameId, ReviewRequest request) { User user=getCurrentUser(); Game game=gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found")); if(reviewRepository.existsByUserIdAndGameId(user.getId(),gameId)) throw new ConflictException("You have already reviewed this game"); Review saved=reviewRepository.save(new Review(user,game,request.rating(),request.title(),request.content())); updateGameRating(gameId); activityService.record(user,UserActivity.ActivityType.REVIEW_POSTED,"Posted a game review",game.getTitle(),"GAME",gameId); return saved; }
    @Transactional(readOnly=true) public List<Review> getReviewsByGame(String gameId) { if(!gameRepository.existsById(gameId)) throw new ResourceNotFoundException("Game not found"); return reviewRepository.findByGameIdOrderByCreatedAtDesc(gameId); }
    @Transactional public Review updateReview(Long reviewId, ReviewRequest request) { User user=getCurrentUser(); Review review=reviewRepository.findById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found")); if(!review.getUser().getId().equals(user.getId())) throw new AccessDeniedException("You are not allowed to update this review"); review.setRating(request.rating()); review.setTitle(request.title()); review.setContent(request.content()); Review updated=reviewRepository.save(review); updateGameRating(review.getGame().getId()); return updated; }
    @Transactional public void deleteReview(Long reviewId) { User user=getCurrentUser(); Review review=reviewRepository.findById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found")); if(!review.getUser().getId().equals(user.getId())) throw new AccessDeniedException("You are not allowed to delete this review"); String gameId=review.getGame().getId(); reviewRepository.delete(review); updateGameRating(gameId); }
    private void updateGameRating(String gameId) { Game game=gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found")); BigDecimal averageRating=reviewRepository.calculateAverageRating(gameId); long reviewCount=reviewRepository.countByGameIdForRating(gameId); game.setRating(averageRating==null ? BigDecimal.ZERO : averageRating.setScale(1,RoundingMode.HALF_UP)); game.setReviewCount((int)reviewCount); gameRepository.save(game); }
    private User getCurrentUser() { Authentication authentication=SecurityContextHolder.getContext().getAuthentication(); String username=authentication.getName(); return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found")); }
}