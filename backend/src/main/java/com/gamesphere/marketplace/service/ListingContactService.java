package com.gamesphere.marketplace.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.marketplace.api.ListingContactRequest;
import com.gamesphere.marketplace.domain.GameListing;
import com.gamesphere.marketplace.repository.GameListingRepository;
import com.gamesphere.notifications.domain.Notification.NotificationType;
import com.gamesphere.notifications.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListingContactService {

    private final GameListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ListingContactService(GameListingRepository listingRepository, UserRepository userRepository,
                                 NotificationService notificationService) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void contact(UUID listingId, ListingContactRequest request) {
        GameListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Game listing not found"));
        if (listing.getStatus() != GameListing.Status.ACTIVE) {
            throw new IllegalArgumentException("This listing is no longer active");
        }

        User buyer = currentUser();
        if (buyer.getId().equals(listing.getSeller().getId())) {
            throw new IllegalArgumentException("You cannot contact yourself about your listing");
        }

        String sender = buyer.getDisplayName() == null || buyer.getDisplayName().isBlank()
                ? buyer.getUsername() : buyer.getDisplayName();
        String message = sender + " is interested in your listing \"" + listing.getTitle()
                + "\". Message: " + request.message();

        notificationService.create(
                listing.getSeller(),
                NotificationType.MARKETPLACE,
                "New buyer inquiry",
                message,
                "GAME_LISTING",
                listing.getId().toString()
        );
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
