package com.gamesphere.reviews.api;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReviewRequest(

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("10.0")
        BigDecimal rating,

        @Size(max = 200)
        String title,

        @Size(max = 5000)
        String content

) {}
