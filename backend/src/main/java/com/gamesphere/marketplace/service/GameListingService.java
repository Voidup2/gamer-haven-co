package com.gamesphere.marketplace.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.marketplace.api.GameListingRequest;
import com.gamesphere.marketplace.api.GameListingResponse;
import com.gamesphere.marketplace.domain.GameListing;
import com.gamesphere.marketplace.repository.GameListingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GameListingService {

    private final GameListingRepository listingRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public GameListingService(GameListingRepository listingRepository, GameRepository gameRepository,
                              UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GameListingResponse create(String gameId, GameListingRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        User seller = currentUser();
        if (request.contactEmail() == null && request.contactPhone() == null) {
            throw new IllegalArgumentException("At least one contact method is required");
        }
        GameListing listing = new GameListing(game, seller, request.title(), request.imageUrl(), request.description(),
                request.condition(), request.price(), request.platform(), request.location(), request.contactEmail(),
                request.contactPhone(), request.boxIncluded(), request.manualIncluded());
        return GameListingResponse.from(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findActive(Pageable pageable) {
        return findActive(null, null, null, null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findActive(String search, String gameId,
                                                GameListing.Condition condition,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                String platform, Boolean boxIncluded,
                                                Boolean manualIncluded, Pageable pageable) {
        Specification<GameListing> specification = Specification.allOf(
                GameListingSpecifications.status(GameListing.Status.ACTIVE),
                GameListingSpecifications.search(search),
                GameListingSpecifications.gameId(gameId),
                GameListingSpecifications.condition(condition),
                GameListingSpecifications.minPrice(minPrice),
                GameListingSpecifications.maxPrice(maxPrice),
                GameListingSpecifications.platform(platform),
                GameListingSpecifications.boxIncluded(boxIncluded),
                GameListingSpecifications.manualIncluded(manualIncluded)
        );
        return listingRepository.findAll(specification, pageable).map(GameListingResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findByGame(String gameId, Pageable pageable) {
        return findActive(null, gameId, null, null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public GameListingResponse findById(UUID id) {
        return GameListingResponse.from(findListing(id));
    }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findMine(Pageable pageable) {
        return listingRepository.findBySellerId(currentUser().getId(), pageable).map(GameListingResponse::from);
    }

    @Transactional
    public GameListingResponse update(UUID id, GameListingRequest request) {
        GameListing listing = findOwned(id);
        if (request.contactEmail() == null && request.contactPhone() == null) {
            throw new IllegalArgumentException("At least one contact method is required");
        }
        listing.update(request.title(), request.imageUrl(), request.description(), request.condition(), request.price(),
                request.platform(), request.location(), request.contactEmail(), request.contactPhone(),
                request.boxIncluded(), request.manualIncluded());
        return GameListingResponse.from(listingRepository.save(listing));
    }

    @Transactional
    public void delete(UUID id) {
        GameListing listing = findOwned(id);
        listing.setStatus(GameListing.Status.REMOVED);
        listingRepository.save(listing);
    }

    @Transactional
    public GameListingResponse updateStatus(UUID id, GameListing.Status status) {
        GameListing listing = findOwned(id);
        listing.setStatus(status);
        return GameListingResponse.from(listingRepository.save(listing));
    }

    private GameListing findOwned(UUID id) {
        GameListing listing = findListing(id);
        if (!listing.getSeller().getId().equals(currentUser().getId())) {
            throw new AccessDeniedException("You are not allowed to modify this listing");
        }
        return listing;
    }

    private GameListing findListing(UUID id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game listing not found"));
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
