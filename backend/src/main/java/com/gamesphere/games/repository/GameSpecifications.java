package com.gamesphere.games.repository;

import com.gamesphere.games.domain.Game;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class GameSpecifications {

    private GameSpecifications() {}

    public static Specification<Game> titleOrDeveloperOrPublisherContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String value = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), value),
                    cb.like(cb.lower(root.get("developer")), value),
                    cb.like(cb.lower(root.get("publisher")), value)
            );
        };
    }

    public static Specification<Game> genre(String value) {
        return jsonArrayContains("genres", value);
    }

    public static Specification<Game> platform(String value) {
        return jsonArrayContains("platforms", value);
    }

    public static Specification<Game> tag(String value) {
        return jsonArrayContains("tags", value);
    }

    private static Specification<Game> jsonArrayContains(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) {
                return cb.conjunction();
            }
            return cb.isTrue(cb.function(
                    "jsonb_exists",
                    Boolean.class,
                    root.get(field),
                    cb.literal(value.trim())
            ));
        };
    }

    public static Specification<Game> releaseYear(Integer year) {
        return (root, query, cb) -> year == null
                ? cb.conjunction()
                : cb.equal(root.get("releaseYear"), year);
    }

    public static Specification<Game> minRating(BigDecimal value) {
        return (root, query, cb) -> value == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("rating"), value);
    }

    public static Specification<Game> maxRating(BigDecimal value) {
        return (root, query, cb) -> value == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("rating"), value);
    }

    public static Specification<Game> minPrice(BigDecimal value) {
        return (root, query, cb) -> value == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("price"), value);
    }

    public static Specification<Game> maxPrice(BigDecimal value) {
        return (root, query, cb) -> value == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("price"), value);
    }

    public static Specification<Game> booleanEquals(String field, Boolean value) {
        return (root, query, cb) -> value == null
                ? cb.conjunction()
                : cb.equal(root.get(field), value);
    }

    public static Specification<Game> releaseDateBefore(LocalDate date) {
        return (root, query, cb) -> date == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("releaseDate"), date);
    }

    public static Specification<Game> releaseDateAfter(LocalDate date) {
        return (root, query, cb) -> date == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("releaseDate"), date);
    }
}
