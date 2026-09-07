package com.gamesphere.marketplace.service;

import com.gamesphere.marketplace.domain.GameListing;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class GameListingSpecifications {

    private GameListingSpecifications() {}

    public static Specification<GameListing> status(GameListing.Status status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<GameListing> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.trim().toLowerCase() + "%";
            var game = root.join("game");
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("platform")), pattern),
                    cb.like(cb.lower(root.get("location")), pattern),
                    cb.like(cb.lower(game.get("title")), pattern)
            );
        };
    }

    public static Specification<GameListing> gameId(String gameId) {
        return (root, query, cb) -> gameId == null || gameId.isBlank()
                ? null : cb.equal(root.get("game").get("id"), gameId);
    }

    public static Specification<GameListing> condition(GameListing.Condition condition) {
        return (root, query, cb) -> condition == null ? null : cb.equal(root.get("condition"), condition);
    }

    public static Specification<GameListing> minPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<GameListing> maxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<GameListing> platform(String platform) {
        return (root, query, cb) -> platform == null || platform.isBlank()
                ? null : cb.equal(cb.lower(root.get("platform")), platform.trim().toLowerCase());
    }

    public static Specification<GameListing> boxIncluded(Boolean value) {
        return (root, query, cb) -> value == null ? null : cb.equal(root.get("boxIncluded"), value);
    }

    public static Specification<GameListing> manualIncluded(Boolean value) {
        return (root, query, cb) -> value == null ? null : cb.equal(root.get("manualIncluded"), value);
    }
}
