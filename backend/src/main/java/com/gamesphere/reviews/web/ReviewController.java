package com.gamesphere.reviews.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.reviews.api.ReviewRequest;
import com.gamesphere.reviews.api.ReviewResponse;
import com.gamesphere.reviews.domain.Review;
import com.gamesphere.reviews.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/games/{gameId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable String gameId,
            @Valid @RequestBody ReviewRequest request
    ) {
        Review review = reviewService.createReview(gameId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Review created",
                        toResponse(review)
                ));
    }

    @GetMapping("/games/{gameId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable String gameId
    ) {
        List<ReviewResponse> reviews = reviewService
                .getReviewsByGame(gameId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reviews retrieved",
                        reviews
                )
        );
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request
    ) {
        Review review =
                reviewService.updateReview(reviewId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review updated",
                        toResponse(review)
                )
        );
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review deleted",
                        null
                )
        );
    }

    private ReviewResponse toResponse(Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getUser().getUsername(),
                review.getGame().getId(),
                review.getRating(),
                review.getTitle(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
