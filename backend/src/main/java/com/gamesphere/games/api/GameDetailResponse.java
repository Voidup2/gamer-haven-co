package com.gamesphere.games.api;

import com.gamesphere.reviews.api.ReviewResponse;

import java.util.List;

public record GameDetailResponse(
        GameResponse game,
        List<ReviewResponse> reviews,
        long reviewCount
) {
    public static GameDetailResponse from(GameResponse game, List<ReviewResponse> reviews) {
        return new GameDetailResponse(game, reviews, reviews.size());
    }
}
