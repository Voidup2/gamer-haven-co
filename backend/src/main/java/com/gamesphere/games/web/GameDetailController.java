package com.gamesphere.games.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.games.api.GameDetailResponse;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.service.GameService;
import com.gamesphere.reviews.api.ReviewResponse;
import com.gamesphere.reviews.domain.Review;
import com.gamesphere.reviews.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
public class GameDetailController {
    private final GameService gameService;
    private final ReviewService reviewService;

    public GameDetailController(GameService gameService, ReviewService reviewService) {
        this.gameService = gameService;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<GameDetailResponse>> details(@PathVariable String id) {
        GameResponse game = gameService.findById(id);
        List<ReviewResponse> reviews = reviewService.getReviewsByGame(id).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(
                "Game details retrieved", GameDetailResponse.from(game, reviews)));
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getUser().getUsername(),
                review.getGame().getId(), review.getRating(), review.getTitle(), review.getContent(),
                review.getCreatedAt(), review.getUpdatedAt());
    }
}
