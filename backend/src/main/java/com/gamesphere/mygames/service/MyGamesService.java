package com.gamesphere.mygames.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.collections.domain.GameCollection;
import com.gamesphere.collections.domain.GameCollectionItem;
import com.gamesphere.collections.repository.GameCollectionItemRepository;
import com.gamesphere.collections.repository.GameCollectionRepository;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.domain.UserGameLibrary;
import com.gamesphere.library.domain.UserGameWishlist;
import com.gamesphere.library.repository.UserGameLibraryRepository;
import com.gamesphere.library.repository.UserGameWishlistRepository;
import com.gamesphere.mygames.api.MyGameResponse;
import com.gamesphere.mygames.api.MyGamesSummaryResponse;
import com.gamesphere.progress.api.GameProgressResponse;
import com.gamesphere.progress.domain.GameProgress;
import com.gamesphere.progress.repository.GameProgressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MyGamesService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameLibraryRepository libraryRepository;
    private final UserGameWishlistRepository wishlistRepository;
    private final GameProgressRepository progressRepository;
    private final GameCollectionRepository collectionRepository;
    private final GameCollectionItemRepository collectionItemRepository;

    public MyGamesService(UserRepository userRepository,
                          GameRepository gameRepository,
                          UserGameLibraryRepository libraryRepository,
                          UserGameWishlistRepository wishlistRepository,
                          GameProgressRepository progressRepository,
                          GameCollectionRepository collectionRepository,
                          GameCollectionItemRepository collectionItemRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.libraryRepository = libraryRepository;
        this.wishlistRepository = wishlistRepository;
        this.progressRepository = progressRepository;
        this.collectionRepository = collectionRepository;
        this.collectionItemRepository = collectionItemRepository;
    }

    @Transactional(readOnly = true)
    public Page<MyGameResponse> mine(String source, Pageable pageable) {
        User user = currentUser();
        Aggregation data = aggregate(user);
        String normalized = source == null || source.isBlank() ? "ALL" : source.trim().toUpperCase(Locale.ROOT);

        Set<String> ids = new LinkedHashSet<>();
        switch (normalized) {
            case "ALL" -> {
                ids.addAll(data.libraryIds);
                ids.addAll(data.wishlistIds);
                ids.addAll(data.progressByGame.keySet());
                ids.addAll(data.collectionNamesByGame.keySet());
            }
            case "LIBRARY" -> ids.addAll(data.libraryIds);
            case "WISHLIST" -> ids.addAll(data.wishlistIds);
            case "PROGRESS" -> ids.addAll(data.progressByGame.keySet());
            case "COLLECTION" -> ids.addAll(data.collectionNamesByGame.keySet());
            default -> throw new IllegalArgumentException("source must be ALL, LIBRARY, WISHLIST, PROGRESS, or COLLECTION");
        }

        List<Game> games = gameRepository.findAllById(ids);
        Map<String, Game> gameById = games.stream().collect(Collectors.toMap(Game::getId, Function.identity()));
        List<MyGameResponse> all = ids.stream()
                .map(gameById::get)
                .filter(Objects::nonNull)
                .map(game -> toResponse(game, data))
                .sorted(Comparator.comparing(r -> r.game().title(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        int start = (int) Math.min((long) pageable.getPageNumber() * pageable.getPageSize(), all.size());
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    @Transactional(readOnly = true)
    public MyGamesSummaryResponse summary() {
        Aggregation data = aggregate(currentUser());
        EnumMap<GameProgress.Status, Long> counts = new EnumMap<>(GameProgress.Status.class);
        for (GameProgress.Status status : GameProgress.Status.values()) {
            counts.put(status, data.progressByStatus.getOrDefault(status, 0L));
        }

        Set<String> library = data.libraryIds;
        long overlap = data.wishlistIds.stream().filter(library::contains).count();
        long collectionGameCount = data.collectionNamesByGame.size();
        OffsetDateTime latest = data.progressByGame.values().stream()
                .map(GameProgress::getLastPlayedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new MyGamesSummaryResponse(
                library.size(),
                data.wishlistIds.size(),
                overlap,
                data.progressByGame.size(),
                counts.get(GameProgress.Status.COMPLETED),
                counts.get(GameProgress.Status.PLAYING),
                counts.get(GameProgress.Status.ON_HOLD),
                counts.get(GameProgress.Status.DROPPED),
                counts.get(GameProgress.Status.NOT_STARTED),
                data.progressByGame.values().stream().mapToLong(GameProgress::getPlaytimeMinutes).sum(),
                data.collections.size(),
                collectionGameCount,
                counts,
                latest
        );
    }

    private Aggregation aggregate(User user) {
        Set<String> libraryIds = libraryRepository.findByUserId(user.getId()).stream()
                .map(UserGameLibrary::getGame).map(Game::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> wishlistIds = wishlistRepository.findByUserId(user.getId()).stream()
                .map(UserGameWishlist::getGame).map(Game::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, GameProgress> progressByGame = progressRepository.findByUserIdOrderByLastPlayedAtDesc(user.getId()).stream()
                .collect(Collectors.toMap(p -> p.getGame().getId(), Function.identity(), (a, b) -> a, LinkedHashMap::new));
        Map<GameProgress.Status, Long> progressByStatus = progressByGame.values().stream()
                .collect(Collectors.groupingBy(GameProgress::getStatus, () -> new EnumMap<>(GameProgress.Status.class), Collectors.counting()));

        List<GameCollection> collections = collectionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Map<String, List<String>> collectionNamesByGame = new HashMap<>();
        for (GameCollection collection : collections) {
            for (GameCollectionItem item : collectionItemRepository.findByCollectionIdOrderByAddedAtDesc(collection.getId())) {
                collectionNamesByGame.computeIfAbsent(item.getGame().getId(), ignored -> new ArrayList<>()).add(collection.getName());
            }
        }
        return new Aggregation(libraryIds, wishlistIds, progressByGame, progressByStatus, collections, collectionNamesByGame);
    }

    private MyGameResponse toResponse(Game game, Aggregation data) {
        List<String> collectionNames = data.collectionNamesByGame.getOrDefault(game.getId(), List.of());
        GameProgress progress = data.progressByGame.get(game.getId());
        return new MyGameResponse(
                GameResponse.from(game),
                data.libraryIds.contains(game.getId()),
                data.wishlistIds.contains(game.getId()),
                !collectionNames.isEmpty(),
                collectionNames,
                progress == null ? null : GameProgressResponse.from(progress)
        );
    }

    private User currentUser() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private record Aggregation(
            Set<String> libraryIds,
            Set<String> wishlistIds,
            Map<String, GameProgress> progressByGame,
            Map<GameProgress.Status, Long> progressByStatus,
            List<GameCollection> collections,
            Map<String, List<String>> collectionNamesByGame
    ) {}
}
