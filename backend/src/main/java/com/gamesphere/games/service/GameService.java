package com.gamesphere.games.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gamesphere.games.api.GameRequest;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gamesphere.common.web.ResourceNotFoundException;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findAll() {
        return gameRepository.findAll().stream().map(GameResponse::from).toList();
    }

    @Transactional(readOnly = true)
        public Page<GameResponse> findAll(
            String search,
            Pageable pageable
) {

    Page<Game> games;

    if (search != null && !search.isBlank()) {
        games = gameRepository.findByTitleContainingIgnoreCase(
                search.trim(),
                pageable
        );
    } else {
        games = gameRepository.findAll(pageable);
    }

    return games.map(GameResponse::from);
}
    @Transactional(readOnly = true)
    public GameResponse findById(String id) {
        return GameResponse.from(findGame(id));
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
