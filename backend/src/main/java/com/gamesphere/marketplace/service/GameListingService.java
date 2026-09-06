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
import com.gamesphere.marketplace.repository.SellerRatingRepository;
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
    private final SellerRatingRepository ratingRepository;

    public GameListingService(GameListingRepository listingRepository, GameRepository gameRepository,
                              UserRepository userRepository, SellerRatingRepository ratingRepository) {
        this.listingRepository = listingRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public GameListingResponse create(String gameId, GameListingRequest request) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        User seller = currentUser();
        requireContact(request);
        GameListing listing = new GameListing(game, seller, request.title(), request.imageUrl(), request.description(),
                request.condition(), request.price(), request.platform(), request.location(), request.contactEmail(),
                request.contactPhone(), request.boxIncluded(), request.manualIncluded());
        return response(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findActive(Pageable pageable) {
        return findActive(null, null, null, null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findActive(String search, String gameId, GameListing.Condition condition,
                                                BigDecimal minPrice, BigDecimal maxPrice, String platform,
                                                Boolean boxIncluded, Boolean manualIncluded, Pageable pageable) {
        Specification<GameListing> specification = Specification.allOf(
                GameListingSpecifications.status(GameListing.Status.ACTIVE),
                GameListingSpecifications.search(search), GameListingSpecifications.gameId(gameId),
                GameListingSpecifications.condition(condition), GameListingSpecifications.minPrice(minPrice),
                GameListingSpecifications.maxPrice(maxPrice), GameListingSpecifications.platform(platform),
                GameListingSpecifications.boxIncluded(boxIncluded), GameListingSpecifications.manualIncluded(manualIncluded));
        return listingRepository.findAll(specification, pageable).map(this::response);
    }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findByGame(String gameId, Pageable pageable) {
        return findActive(null, gameId, null, null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public GameListingResponse findById(UUID id) { return response(findListing(id)); }

    @Transactional(readOnly = true)
    public Page<GameListingResponse> findMine(Pageable pageable) {
        return listingRepository.findBySellerId(currentUser().getId(), pageable).map(this::response);
    }

    @Transactional
    public GameListingResponse update(UUID id, GameListingRequest request) {
        GameListing listing = findOwned(id);
        requireContact(request);
        listing.update(request.title(), request.imageUrl(), request.description(), request.condition(), request.price(),
                request.platform(), request.location(), request.contactEmail(), request.contactPhone(),
                request.boxIncluded(), request.manualIncluded());
        return response(listingRepository.save(listing));
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
        return response(listingRepository.save(listing));
    }

    private GameListingResponse response(GameListing listing) {
        Long sellerId = listing.getSeller().getId();
        Double average = ratingRepository.findAverageRatingBySellerId(sellerId);
        long count = ratingRepository.countBySellerId(sellerId);
        return GameListingResponse.from(listing, average == null ? 0.0 : average, count);
    }

    private void requireContact(GameListingRequest request) {
        if ((request.contactEmail() == null || request.contactEmail().isBlank())
                && (request.contactPhone() == null || request.contactPhone().isBlank())) {
            throw new IllegalArgumentException("At least one contact method is required");
        }
    }

    private GameListing findOwned(UUID id) {
        GameListing listing = findListing(id);
        if (!listing.getSeller().getId().equals(currentUser().getId())) {
            throw new AccessDeniedException("You are not allowed to modify this listing");
        }
        return listing;
    }

    private GameListing findListing(UUID id) {
        return listingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Game listing not found"));
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
