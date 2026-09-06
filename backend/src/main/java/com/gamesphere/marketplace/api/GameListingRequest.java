package com.gamesphere.marketplace.api;

import com.gamesphere.marketplace.domain.GameListing.Condition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GameListingRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String imageUrl,
        @NotBlank @Size(max = 10000) String description,
        @NotNull Condition condition,
        @NotNull @DecimalMin(value = "0.00") BigDecimal price,
        @NotBlank @Size(max = 50) String platform,
        @Size(max = 200) String location,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 30) String contactPhone,
        boolean boxIncluded,
        boolean manualIncluded
) {}
