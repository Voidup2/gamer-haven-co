package com.gamesphere.games.api;

import com.gamesphere.games.domain.Game;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GameRequest(
        @NotBlank @Size(max = 100) String id,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 300) String tagline,
        @NotBlank String description,
        String coverUrl,
        String bannerUrl,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal rating,
        @NotNull @Min(0) Integer reviewCount,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Min(0) @DecimalMax("100") Integer discount,
        LocalDate releaseDate,
        @Min(1970) Integer releaseYear,
        @Size(max = 150) String developer,
        @Size(max = 150) String publisher,
        @Size(max = 50) String esrb,
        boolean multiplayer,
        boolean coop,
        boolean freeToPlay,
        boolean vr,
        boolean earlyAccess,
        boolean controller,
        List<String> genres,
        List<String> platforms,
        List<String> tags,
        List<String> languages,
        List<String> features,
        List<Game.StoreLink> stores,
        List<Game.Requirement> requirements
) {}
