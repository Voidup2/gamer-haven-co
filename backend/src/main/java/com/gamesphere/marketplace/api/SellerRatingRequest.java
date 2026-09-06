package com.gamesphere.marketplace.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SellerRatingRequest(
        @Min(1) @Max(5) short rating,
        @Size(max = 2000) String review
) {}
