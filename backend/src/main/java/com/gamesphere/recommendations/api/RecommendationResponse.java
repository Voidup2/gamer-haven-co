package com.gamesphere.recommendations.api;

import com.gamesphere.games.api.GameResponse;

import java.math.BigDecimal;

public record RecommendationResponse(
        GameResponse game,
        BigDecimal score,
        String reason
) {}
