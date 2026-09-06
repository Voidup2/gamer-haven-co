package com.gamesphere.collections.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.collections.api.GameCollectionItemResponse;
import com.gamesphere.collections.api.GameCollectionRequest;
import com.gamesphere.collections.api.GameCollectionResponse;
import com.gamesphere.collections.domain.GameCollection;
import com.gamesphere.collections.domain.GameCollectionItem;
import com.gamesphere.collections.repository.GameCollectionItemRepository;
import com.gamesphere.collections.repository.GameCollectionRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GameCollectionService {
    private final GameCollectionRepository collectionRepository;
    private final GameCollectionItemRepository itemRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public GameCollectionService(GameCollectionRepository collectionRepository,
                                 GameCollectionItemRepository itemRepository,
                                 GameRepository gameRepository,
                                 UserRepository userRepository) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GameCollectionResponse create(GameCollectionRequest request) {
        User user = currentUser();
        if (collectionRepository.existsByUserIdAndName(user.getId(), request.name())) {
            throw new ConflictException("A collection with this name already exists");
        }
        return response(collectionRepository.save(new GameCollection(user, request.name(), request.description(), request.publicCollection())));
    }

    @Transactional(readOnly = true)
    public List<GameCollectionResponse> mine() {
        return collectionRepository.findByUserIdOrderByCreatedAtDesc(currentUser().getId()).stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<GameCollectionResponse> publicCollections() {
        return collectionRepository.findByPublicCollectionTrueOrderByCreatedAtDesc().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public GameCollectionResponse get(UUID id) {
        GameCollection collection = findVisible(id);
        return response(collection);
    }

    @Transactional
    public GameCollectionResponse update(UUID id, GameCollectionRequest request) {
        GameCollection collection = owned(id);
        if (collectionRepository.existsByUserIdAndNameAndIdNot(collection.getUser().getId(), request.name(), id)) {
            throw new ConflictException("A collection with this name already exists");
        }
        collection.update(request.name(), request.description(), request.publicCollection());
        return response(collectionRepository.save(collection));
    }

    @Transactional
    public void delete(UUID id) {
        collectionRepository.delete(owned(id));
    }

    @Transactional
    public GameCollectionItemResponse addGame(UUID id, String gameId) {
        GameCollection collection = owned(id);
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        if (itemRepository.existsByCollectionIdAndGameId(id, gameId)) {
            throw new ConflictException("Game is already in this collection");
        }
        return GameCollectionItemResponse.from(itemRepository.save(new GameCollectionItem(collection, game)));
    }

    @Transactional(readOnly = true)
    public List<GameCollectionItemResponse> games(UUID id) {
        GameCollection collection = findVisible(id);
        return itemRepository.findByCollectionIdOrderByAddedAtDesc(collection.getId()).stream().map(GameCollectionItemResponse::from).toList();
    }

    @Transactional
    public void removeGame(UUID id, String gameId) {
        owned(id);
        GameCollectionItem item = itemRepository.findByCollectionIdAndGameId(id, gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game is not in this collection"));
        itemRepository.delete(item);
    }

    private GameCollection findVisible(UUID id) {
        GameCollection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        User user = currentUserOptional();
        if (!collection.isPublicCollection() && (user == null || !collection.getUser().getId().equals(user.getId()))) {
            throw new AccessDeniedException("This collection is private");
        }
        return collection;
    }

    private GameCollection owned(UUID id) {
        GameCollection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));
        if (!collection.getUser().getId().equals(currentUser().getId())) {
            throw new AccessDeniedException("You are not allowed to modify this collection");
        }
        return collection;
    }

    private GameCollectionResponse response(GameCollection collection) {
        return GameCollectionResponse.from(collection, itemRepository.findByCollectionIdOrderByAddedAtDesc(collection.getId()).size());
    }

    private User currentUser() {
        User user = currentUserOptional();
        if (user == null) throw new AccessDeniedException("Authentication required");
        return user;
    }

    private User currentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) return null;
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}