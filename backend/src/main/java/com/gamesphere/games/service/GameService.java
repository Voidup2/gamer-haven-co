package com.gamesphere.games.service;

import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.api.GameRequest;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.games.repository.GameSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional(readOnly = true)
    public Page<GameResponse> search(
            String search,
            String genre,
            String platform,
            String tag,
            Integer releaseYear,
            BigDecimal minRating,
            BigDecimal maxRating,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean multiplayer,
            Boolean coop,
            Boolean freeToPlay,
            Boolean vr,
            Boolean earlyAccess,
            Boolean controller,
            LocalDate releaseAfter,
            LocalDate releaseBefore,
            Pageable pageable) {

        Specification<Game> specification = Specification.where(
                        GameSpecifications.titleOrDeveloperOrPublisherContains(search))
                .and(GameSpecifications.genre(genre))
                .and(GameSpecifications.platform(platform))
                .and(GameSpecifications.tag(tag))
                .and(GameSpecifications.releaseYear(releaseYear))
                .and(GameSpecifications.minRating(minRating))
                .and(GameSpecifications.maxRating(maxRating))
                .and(GameSpecifications.minPrice(minPrice))
                .and(GameSpecifications.maxPrice(maxPrice))
                .and(GameSpecifications.booleanEquals("multiplayer", multiplayer))
                .and(GameSpecifications.booleanEquals("coop", coop))
                .and(GameSpecifications.booleanEquals("freeToPlay", freeToPlay))
                .and(GameSpecifications.booleanEquals("vr", vr))
                .and(GameSpecifications.booleanEquals("earlyAccess", earlyAccess))
                .and(GameSpecifications.booleanEquals("controller", controller))
                .and(GameSpecifications.releaseDateAfter(releaseAfter))
                .and(GameSpecifications.releaseDateBefore(releaseBefore));

        return gameRepository.findAll(specification, pageable).map(GameResponse::from);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findAll() {
        return gameRepository.findAll().stream().map(GameResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GameResponse findById(String id) {
        return GameResponse.from(findGame(id));
    }

    @Transactional(readOnly = true)
    public Page<GameResponse> trending(Pageable pageable) {
        List<Game> games = new ArrayList<>(gameRepository.findAll());
        games.sort(Comparator.comparingDouble(this::trendingScore)
                .reversed()
                .thenComparing(Game::getTitle, String.CASE_INSENSITIVE_ORDER));
        return page(games, pageable);
    }

    @Transactional(readOnly = true)
    public Page<GameResponse> topRated(Pageable pageable) {
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : pageable.withSort(Sort.by(Sort.Direction.DESC, "rating"));
        return gameRepository.findAll(sorted).map(GameResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<GameResponse> recentlyReleased(Pageable pageable) {
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : pageable.withSort(Sort.by(Sort.Direction.DESC, "releaseDate"));
        return gameRepository.findAll(sorted).map(GameResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<GameResponse> upcoming(Pageable pageable) {
        Specification<Game> specification = GameSpecifications.releaseDateAfter(LocalDate.now().minusDays(1));
        return gameRepository.findAll(specification,
                pageable.getSort().isSorted()
                        ? pageable
                        : pageable.withSort(Sort.by(Sort.Direction.ASC, "releaseDate")))
                .map(GameResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<GameResponse> related(String id, Pageable pageable) {
        Game source = findGame(id);
        Set<String> sourceGenres = lowerCase(source.getGenres());
        Set<String> sourcePlatforms = lowerCase(source.getPlatforms());

        List<Game> candidates = gameRepository.findAll().stream()
                .filter(game -> !game.getId().equals(source.getId()))
                .filter(game -> overlaps(game.getGenres(), sourceGenres) || overlaps(game.getPlatforms(), sourcePlatforms))
                .sorted(Comparator.comparingInt((Game game) -> relatedScore(game, sourceGenres, sourcePlatforms))
                        .reversed()
                        .thenComparing(Game::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return page(candidates, pageable);
    }

    @Transactional
    public GameResponse create(GameRequest request) {
        if (gameRepository.existsById(request.id())) {
            throw new IllegalArgumentException("Game id is already in use");
        }
        Game game = new Game();
        apply(game, request);
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public GameResponse update(String id, GameRequest request) {
        Game game = findGame(id);
        if (!id.equals(request.id())) {
            throw new IllegalArgumentException("Game id cannot be changed");
        }
        apply(game, request);
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public void delete(String id) {
        Game game = findGame(id);
        gameRepository.delete(game);
    }

    private double trendingScore(Game game) {
        double rating = game.getRating() == null ? 0.0 : game.getRating().doubleValue();
        double reviews = game.getReviewCount() == null ? 0.0 : game.getReviewCount();
        return (rating * 0.7) + (Math.log1p(reviews) * 0.3);
    }

    private int relatedScore(Game game, Set<String> sourceGenres, Set<String> sourcePlatforms) {
        int genreMatches = (int) game.getGenres().stream()
                .map(String::toLowerCase)
                .filter(sourceGenres::contains)
                .count();
        int platformMatches = (int) game.getPlatforms().stream()
                .map(String::toLowerCase)
                .filter(sourcePlatforms::contains)
                .count();
        double rating = game.getRating() == null ? 0.0 : game.getRating().doubleValue();
        return (genreMatches * 10) + (platformMatches * 5) + (int) Math.round(rating);
    }

    private boolean overlaps(List<String> values, Set<String> target) {
        return values.stream().map(String::toLowerCase).anyMatch(target::contains);
    }

    private Set<String> lowerCase(List<String> values) {
        Set<String> result = new HashSet<>();
        values.forEach(value -> result.add(value.toLowerCase()));
        return result;
    }

    private Page<GameResponse> page(List<Game> games, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= games.size()) {
            return new PageImpl<>(List.of(), pageable, games.size());
        }
        int end = Math.min(start + pageable.getPageSize(), games.size());
        return new PageImpl<>(games.subList(start, end).stream().map(GameResponse::from).toList(),
                pageable, games.size());
    }

    private Game findGame(String id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
    }

    private void apply(Game game, GameRequest request) {
        game.setId(request.id());
        game.setTitle(request.title());
        game.setTagline(request.tagline());
        game.setDescription(request.description());
        game.setCoverUrl(request.coverUrl());
        game.setBannerUrl(request.bannerUrl());
        game.setRating(request.rating());
        game.setReviewCount(request.reviewCount());
        game.setPrice(request.price());
        game.setDiscount(request.discount());
        game.setReleaseDate(request.releaseDate());
        game.setReleaseYear(request.releaseYear());
        game.setDeveloper(request.developer());
        game.setPublisher(request.publisher());
        game.setEsrb(request.esrb());
        game.setMultiplayer(request.multiplayer());
        game.setCoop(request.coop());
        game.setFreeToPlay(request.freeToPlay());
        game.setVr(request.vr());
        game.setEarlyAccess(request.earlyAccess());
        game.setController(request.controller());
        game.setGenres(request.genres() == null ? List.of() : request.genres());
        game.setPlatforms(request.platforms() == null ? List.of() : request.platforms());
        game.setTags(request.tags() == null ? List.of() : request.tags());
        game.setLanguages(request.languages() == null ? List.of() : request.languages());
        game.setFeatures(request.features() == null ? List.of() : request.features());
        game.setStores(request.stores() == null ? List.of() : request.stores());
        game.setRequirements(request.requirements() == null ? List.of() : request.requirements());
    }
}
