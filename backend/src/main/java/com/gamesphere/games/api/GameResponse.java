package com.gamesphere.games.api;

import com.gamesphere.games.domain.Game;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record GameResponse(
        String id,
        String title,
        String tagline,
        String description,
        String coverUrl,
        String bannerUrl,
        BigDecimal rating,
        Integer reviewCount,
        BigDecimal price,
        Integer discount,
        LocalDate releaseDate,
        Integer releaseYear,
        String developer,
        String publisher,
        String esrb,
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
        List<Game.Requirement> requirements,
        Instant createdAt,
        Instant updatedAt
) {
    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(), game.getTitle(), game.getTagline(), game.getDescription(),
                game.getCoverUrl(), game.getBannerUrl(), game.getRating(), game.getReviewCount(),
                game.getPrice(), game.getDiscount(), game.getReleaseDate(), game.getReleaseYear(),
                game.getDeveloper(), game.getPublisher(), game.getEsrb(), game.isMultiplayer(),
                game.isCoop(), game.isFreeToPlay(), game.isVr(), game.isEarlyAccess(),
                game.isController(), game.getGenres(), game.getPlatforms(), game.getTags(),
                game.getLanguages(), game.getFeatures(), game.getStores(), game.getRequirements(),
                game.getCreatedAt(), game.getUpdatedAt()
        );
    }
}
