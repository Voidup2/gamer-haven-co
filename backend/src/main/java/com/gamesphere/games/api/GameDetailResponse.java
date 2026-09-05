package com.gamesphere.games.api;

import com.gamesphere.games.domain.Game;
import com.gamesphere.reviews.api.ReviewResponse;

import java.util.List;

public record GameDetailResponse(
        GameResponse game,
        List<ReviewResponse> reviews,
        long reviewCount
) {
    public static GameDetailResponse from(Game game, List<ReviewResponse> reviews) {
        return new GameDetailResponse(game == null ? null : GameResponse.from(game), reviews, reviews.size());
    }
}
