package com.gamesphere.marketplace.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.marketplace.api.SellerRatingRequest;
import com.gamesphere.marketplace.api.SellerRatingResponse;
import com.gamesphere.marketplace.domain.GameListing;
import com.gamesphere.marketplace.domain.SellerRating;
import com.gamesphere.marketplace.repository.GameListingRepository;
import com.gamesphere.marketplace.repository.SellerRatingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SellerRatingService {
    private final SellerRatingRepository ratingRepository;
    private final GameListingRepository listingRepository;
    private final UserRepository userRepository;

    public SellerRatingService(SellerRatingRepository ratingRepository, GameListingRepository listingRepository,
                               UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SellerRatingResponse create(UUID listingId, SellerRatingRequest request) {
        User reviewer = currentUser();
        GameListing listing = findListing(listingId);
        if (listing.getSeller().getId().equals(reviewer.getId())) {
            throw new AccessDeniedException("You cannot rate yourself");
        }
        if (ratingRepository.findByListingIdAndReviewerId(listingId, reviewer.getId()).isPresent()) {
            throw new ConflictException("You have already rated this seller for this listing");
        }
        SellerRating rating = new SellerRating(listing, listing.getSeller(), reviewer, request.rating(), request.review());
        return SellerRatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional(readOnly = true)
    public Page<SellerRatingResponse> findBySeller(Long sellerId, Pageable pageable) {
        if (!userRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("Seller not found");
        }
        return ratingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable).map(SellerRatingResponse::from);
    }

    @Transactional(readOnly = true)
    public SellerRatingResponse findById(UUID id) {
        return SellerRatingResponse.from(ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller rating not found")));
    }

    @Transactional
    public SellerRatingResponse update(UUID id, SellerRatingRequest request) {
        SellerRating rating = findOwned(id);
        rating.update(request.rating(), request.review());
        return SellerRatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional
    public void delete(UUID id) {
        ratingRepository.delete(findOwned(id));
    }

    @Transactional(readOnly = true)
    public SellerRatingSummary summary(Long sellerId) {
        if (!userRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("Seller not found");
        }
        Double average = ratingRepository.findAverageRatingBySellerId(sellerId);
        return new SellerRatingSummary(average == null ? 0.0 : average, ratingRepository.countBySellerId(sellerId));
    }

    private SellerRating findOwned(UUID id) {
        SellerRating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller rating not found"));
        if (!rating.getReviewer().getId().equals(currentUser().getId())) {
            throw new AccessDeniedException("You are not allowed to modify this rating");
        }
        return rating;
    }

    private GameListing findListing(UUID id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game listing not found"));
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public record SellerRatingSummary(double averageRating, long reviewCount) {}
}
