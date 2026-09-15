package com.gamesphere.marketplace;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.marketplace.api.GameListingRequest;
import com.gamesphere.marketplace.domain.GameListing;
import com.gamesphere.marketplace.repository.GameListingRepository;
import com.gamesphere.marketplace.repository.SellerRatingRepository;
import com.gamesphere.marketplace.service.GameListingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameListingServiceTest {
    @Mock GameListingRepository listingRepository;
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock SellerRatingRepository ratingRepository;
    @Mock Authentication authentication;
    @Mock User currentUser;
    @Mock User seller;
    @Mock GameListing listing;

    @InjectMocks GameListingService listingService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanMarkActiveListingSold() {
        UUID id = UUID.randomUUID();
        authenticateAs("seller", 1L);
        givenOwnedListing(id, GameListing.Status.ACTIVE);

        when(listingRepository.save(listing)).thenReturn(listing);
        when(ratingRepository.findAverageRatingBySellerId(1L)).thenReturn(null);
        when(ratingRepository.countBySellerId(1L)).thenReturn(0L);

        listingService.updateStatus(id, GameListing.Status.SOLD);

        verify(listing).setStatus(GameListing.Status.SOLD);
        verify(listingRepository).save(listing);
    }

    @Test
    void ownerCanRemoveActiveListing() {
        UUID id = UUID.randomUUID();
        authenticateAs("seller", 1L);
        givenOwnedListing(id, GameListing.Status.ACTIVE);
        when(listingRepository.save(listing)).thenReturn(listing);
        when(ratingRepository.findAverageRatingBySellerId(1L)).thenReturn(null);
        when(ratingRepository.countBySellerId(1L)).thenReturn(0L);

        listingService.updateStatus(id, GameListing.Status.REMOVED);

        verify(listing).setStatus(GameListing.Status.REMOVED);
    }

    @Test
    void ownerCanRestoreRemovedListing() {
        UUID id = UUID.randomUUID();
        authenticateAs("seller", 1L);
        givenOwnedListing(id, GameListing.Status.REMOVED);
        when(listingRepository.save(listing)).thenReturn(listing);
        when(ratingRepository.findAverageRatingBySellerId(1L)).thenReturn(null);
        when(ratingRepository.countBySellerId(1L)).thenReturn(0L);

        listingService.updateStatus(id, GameListing.Status.ACTIVE);

        verify(listing).setStatus(GameListing.Status.ACTIVE);
    }

    @Test
    void ownerCannotChangeSoldListingBackToActive() {
        UUID id = UUID.randomUUID();
        authenticateAs("seller", 1L);
        givenOwnedListing(id, GameListing.Status.SOLD);

        assertThrows(ConflictException.class,
                () -> listingService.updateStatus(id, GameListing.Status.ACTIVE));
        verify(listing, never()).setStatus(any());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void ownerCannotRemoveSoldListing() {
        UUID id = UUID.randomUUID();
        authenticateAs("seller", 1L);
        givenOwnedListing(id, GameListing.Status.SOLD);

        assertThrows(ConflictException.class,
                () -> listingService.updateStatus(id, GameListing.Status.REMOVED));
        verify(listing, never()).setStatus(any());
    }

    @Test
    void ownerCannotRepeatSameStatus() {
        UUID id = UUID.randomUUID();
        authenticateAs("seller", 1L);
        givenOwnedListing(id, GameListing.Status.ACTIVE);

        assertThrows(ConflictException.class,
                () -> listingService.updateStatus(id, GameListing.Status.ACTIVE));
        verify(listing, never()).setStatus(any());
    }

    @Test
    void nonOwnerCannotUpdateListingStatus() {
        UUID id = UUID.randomUUID();
        authenticateAs("player", 2L);
        when(listingRepository.findById(id)).thenReturn(Optional.of(listing));
        when(listing.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);

        assertThrows(AccessDeniedException.class,
                () -> listingService.updateStatus(id, GameListing.Status.SOLD));
        verify(listing, never()).setStatus(any());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createRejectsUnknownGame() {
        authenticateAs("seller", 1L);
        when(gameRepository.findById("missing-game")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> listingService.create("missing-game", requestWithEmail()));
        verify(listingRepository, never()).save(any());
    }

    @Test
    void createRejectsListingWithoutContactMethod() {
        authenticateAs("seller", 1L);
        when(gameRepository.findById("game-1")).thenReturn(Optional.of(mock(com.gamesphere.games.domain.Game.class)));

        GameListingRequest request = new GameListingRequest(
                "Used Game", null, "Good condition", GameListing.Condition.GOOD,
                new BigDecimal("25.00"), "PS5", null, null, null, false, false);

        assertThrows(IllegalArgumentException.class,
                () -> listingService.create("game-1", request));
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateRejectsNonOwner() {
        UUID id = UUID.randomUUID();
        authenticateAs("player", 2L);
        when(listingRepository.findById(id)).thenReturn(Optional.of(listing));
        when(listing.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);

        assertThrows(AccessDeniedException.class,
                () -> listingService.update(id, requestWithEmail()));
        verify(listing, never()).update(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean(), anyBoolean());
        verify(listingRepository, never()).save(any());
    }

    @Test
    void updateRejectsMissingContactMethod() {
        UUID id = UUID.randomUUID();
        authenticateAs("seller", 1L);
        givenOwnedListing(id, GameListing.Status.ACTIVE);

        GameListingRequest request = new GameListingRequest(
                "Updated Game", null, "Updated description", GameListing.Condition.GOOD,
                new BigDecimal("30.00"), "PS5", null, null, null, false, false);

        assertThrows(IllegalArgumentException.class, () -> listingService.update(id, request));
        verify(listing, never()).update(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean(), anyBoolean());
        verify(listingRepository, never()).save(any());
    }

    private void givenOwnedListing(UUID id, GameListing.Status status) {
        when(listingRepository.findById(id)).thenReturn(Optional.of(listing));
        when(listing.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);
        when(currentUser.getId()).thenReturn(1L);
        when(listing.getStatus()).thenReturn(status);
    }

    private void authenticateAs(String username, long userId) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));
        when(currentUser.getId()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private GameListingRequest requestWithEmail() {
        return new GameListingRequest(
                "Used Game", null, "Good condition", GameListing.Condition.GOOD,
                new BigDecimal("25.00"), "PS5", "Patna", "seller@example.com", null, false, false);
    }
}
