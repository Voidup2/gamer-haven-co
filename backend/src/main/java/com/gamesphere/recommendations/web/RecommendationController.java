package com.gamesphere.recommendations.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.recommendations.api.RecommendationResponse;
import com.gamesphere.recommendations.service.RecommendationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@PreAuthorize("isAuthenticated()")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RecommendationResponse>>> recommend(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Recommendations retrieved successfully",
                recommendationService.recommend(PageRequest.of(page, size))));
    }
}
