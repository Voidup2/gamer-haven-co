package com.gamesphere.marketplace.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ListingContactRequest(
        @NotBlank @Size(max = 2000) String message
) {}
