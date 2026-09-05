package com.gamesphere.reviews.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReviewResponse(

        Long id,

        String username,

        String gameId,

        BigDecimal rating,

        String title,

        String content,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {}
