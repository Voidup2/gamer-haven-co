package com.gamesphere.marketplace;

import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.auth.domain.Role;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.marketplace.api.MarketplaceTransactionResponse;
import com.gamesphere.marketplace.domain.GameListing;
import com.gamesphere.marketplace.domain.MarketplaceTransaction;
import com.gamesphere.marketplace.repository.GameListingRepository;
import com.gamesphere.marketplace.repository.MarketplaceTransactionRepository;
import com.gamesphere.marketplace.service.MarketplaceTransactionService;
import com.gamesphere.notifications.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MarketplaceTransactionServiceTest {
    @Mock MarketplaceTransactionRepository transactionRepository;
    @Mock GameListingRepository listingRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @Mock UserActivityService activityService;
    @Mock Authentication authentication;
    @Mock User buyer;
    @Mock User seller;
    @Mock User admin;
    @Mock User otherUser;
    @Mock GameListing listing;
    @Mock Game game;
    @Mock MarketplaceTransaction transaction;
    @Mock Role adminRole;

    @InjectMocks MarketplaceTransactionService transactionService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initiateCreatesPendingTransaction() {
        UUID listingId = UUID.randomUUID();
        authenticateAs("buyer", 2L, buyer);
        givenListing(listingId, GameListing.Status.ACTIVE);
        when(listing.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);
        when(seller.getUsername()).thenReturn("seller");
        when(buyer.getUsername()).thenReturn("buyer");
        when(listing.getTitle()).thenReturn("Test Game");
        when(listing.getPrice()).thenReturn(new BigDecimal("25.00"));
        when(transactionRepository.existsByListingIdAndStatus(listingId, MarketplaceTransaction.Status.PENDING)).thenReturn(false);
        when(transactionRepository.existsByListingIdAndBuyerId(listingId, 2L)).thenReturn(false);
        MarketplaceTransaction saved = mock(MarketplaceTransaction.class);
        givenResponseFields(saved, listing, buyer, seller, MarketplaceTransaction.Status.PENDING);
        when(transactionRepository.save(any(MarketplaceTransaction.class))).thenReturn(saved);

        MarketplaceTransactionResponse response = transactionService.initiate(listingId);

        assertEquals(MarketplaceTransaction.Status.PENDING, response.status());
        assertEquals(new BigDecimal("25.00"), response.amount());
        verify(transactionRepository).save(any(MarketplaceTransaction.class));
        verify(notificationService).create(eq(seller), any(), eq("New purchase request"), contains("wants to purchase"), eq("MARKETPLACE_TRANSACTION"), anyString());
    }

    @Test
    void initiateRejectsUnknownListing() {
        UUID listingId = UUID.randomUUID();
        authenticateWithoutUserId("buyer", buyer);
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.initiate(listingId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void initiateRejectsInactiveListing() {
        UUID listingId = UUID.randomUUID();
        authenticateWithoutUserId("buyer", buyer);
        givenListing(listingId, GameListing.Status.SOLD);

        assertThrows(ConflictException.class, () -> transactionService.initiate(listingId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void initiateRejectsOwnListing() {
        UUID listingId = UUID.randomUUID();
        authenticateAs("seller", 1L, seller);
        givenListing(listingId, GameListing.Status.ACTIVE);
        when(listing.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);

        assertThrows(ConflictException.class, () -> transactionService.initiate(listingId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void initiateRejectsExistingPendingTransaction() {
        UUID listingId = UUID.randomUUID();
        authenticateAs("buyer", 2L, buyer);
        givenListing(listingId, GameListing.Status.ACTIVE);
        when(listing.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);
        when(transactionRepository.existsByListingIdAndStatus(listingId, MarketplaceTransaction.Status.PENDING)).thenReturn(true);

        assertThrows(ConflictException.class, () -> transactionService.initiate(listingId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void initiateRejectsExistingBuyerTransaction() {
        UUID listingId = UUID.randomUUID();
        authenticateAs("buyer", 2L, buyer);
        givenListing(listingId, GameListing.Status.ACTIVE);
        when(listing.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(1L);
        when(transactionRepository.existsByListingIdAndStatus(listingId, MarketplaceTransaction.Status.PENDING)).thenReturn(false);
        when(transactionRepository.existsByListingIdAndBuyerId(listingId, 2L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> transactionService.initiate(listingId));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void findByIdRejectsUnrelatedUser() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("other", otherUser);
        givenAuthorizationTransaction(id, 1L, 2L, MarketplaceTransaction.Status.PENDING);

        assertThrows(AccessDeniedException.class, () -> transactionService.findById(id));
    }

    @Test
    void findByIdAllowsBuyer() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("buyer", buyer);
        when(buyer.getId()).thenReturn(2L);
        MarketplaceTransactionResponse expected = givenResponseTransaction(id, 1L, 2L, MarketplaceTransaction.Status.PENDING);

        MarketplaceTransactionResponse actual = transactionService.findById(id);

        assertEquals(expected.id(), actual.id());
    }

    @Test
    void completeRejectsNonSeller() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("buyer", buyer);
        when(buyer.getId()).thenReturn(2L);
        givenAuthorizationTransaction(id, 1L, 2L, MarketplaceTransaction.Status.PENDING);

        assertThrows(AccessDeniedException.class, () -> transactionService.complete(id));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void completeRejectsNonPendingTransaction() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("seller", seller);
        when(seller.getId()).thenReturn(1L);
        givenAuthorizationTransaction(id, 1L, 2L, MarketplaceTransaction.Status.CANCELLED);

        assertThrows(ConflictException.class, () -> transactionService.complete(id));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void completeRejectsInactiveListing() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("seller", seller);
        when(seller.getId()).thenReturn(1L);
        givenAuthorizationTransaction(id, 1L, 2L, MarketplaceTransaction.Status.PENDING);
        when(transaction.getListing()).thenReturn(listing);
        when(listing.getStatus()).thenReturn(GameListing.Status.REMOVED);

        assertThrows(ConflictException.class, () -> transactionService.complete(id));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void completeMarksTransactionAndListingSold() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("seller", seller);
        when(seller.getId()).thenReturn(1L);
        when(seller.getUsername()).thenReturn("seller");
        when(buyer.getId()).thenReturn(2L);
        when(buyer.getUsername()).thenReturn("buyer");
        givenAuthorizationTransaction(id, 1L, 2L, MarketplaceTransaction.Status.PENDING);
        when(transaction.getListing()).thenReturn(listing);
        when(listing.getStatus()).thenReturn(GameListing.Status.ACTIVE);
        when(listing.getTitle()).thenReturn("Test Game");
        when(listing.getId()).thenReturn(UUID.randomUUID());
        when(listing.getGame()).thenReturn(game);
        when(game.getId()).thenReturn("game-1");
        when(transaction.getAmount()).thenReturn(new BigDecimal("25.00"));
        when(transaction.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(transaction.getUpdatedAt()).thenReturn(OffsetDateTime.now());
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        doAnswer(invocation -> {
            when(transaction.getStatus()).thenReturn(MarketplaceTransaction.Status.COMPLETED);
            return null;
        }).when(transaction).setStatus(MarketplaceTransaction.Status.COMPLETED);

        transactionService.complete(id);

        verify(transaction).setStatus(MarketplaceTransaction.Status.COMPLETED);
        verify(listing).setStatus(GameListing.Status.SOLD);
        verify(listingRepository).save(listing);
        verify(notificationService).create(eq(buyer), any(), eq("Purchase completed"), contains("was completed"), eq("MARKETPLACE_TRANSACTION"), eq(id.toString()));
        verify(activityService).record(eq(buyer), any(), eq("Completed marketplace purchase"), eq("Test Game"), eq("MARKETPLACE_TRANSACTION"), eq(id.toString()));
        verify(activityService).record(eq(seller), any(), eq("Completed marketplace sale"), eq("Test Game"), eq("MARKETPLACE_TRANSACTION"), eq(id.toString()));
    }

    @Test
    void cancelRejectsUnrelatedUser() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("other", otherUser);
        givenAuthorizationTransaction(id, 1L, 2L, MarketplaceTransaction.Status.PENDING);

        assertThrows(AccessDeniedException.class, () -> transactionService.cancel(id));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cancelAllowsBuyer() {
        UUID id = UUID.randomUUID();
        authenticateWithoutUserId("buyer", buyer);
        when(buyer.getId()).thenReturn(2L);
        when(seller.getId()).thenReturn(1L);
        when(seller.getUsername()).thenReturn("seller");
        givenAuthorizationTransaction(id, 1L, 2L, MarketplaceTransaction.Status.PENDING);
        when(transaction.getListing()).thenReturn(listing);
        when(listing.getTitle()).thenReturn("Test Game");
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        doAnswer(invocation -> {
            when(transaction.getStatus()).thenReturn(MarketplaceTransaction.Status.CANCELLED);
            return null;
        }).when(transaction).setStatus(MarketplaceTransaction.Status.CANCELLED);
        when(transaction.getAmount()).thenReturn(new BigDecimal("25.00"));
        when(transaction.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(transaction.getUpdatedAt()).thenReturn(OffsetDateTime.now());
        when(listing.getId()).thenReturn(UUID.randomUUID());
        when(listing.getGame()).thenReturn(game);
        when(game.getId()).thenReturn("game-1");
        when(buyer.getUsername()).thenReturn("buyer");

        MarketplaceTransactionResponse response = transactionService.cancel(id);

        verify(transaction).setStatus(MarketplaceTransaction.Status.CANCELLED);
        verify(notificationService).create(eq(seller), any(), eq("Transaction cancelled"), contains("was cancelled"), eq("MARKETPLACE_TRANSACTION"), eq(id.toString()));
        assertEquals(MarketplaceTransaction.Status.CANCELLED, response.status());
    }

    @Test
    void findMineAsBuyerUsesBuyerRepositoryQuery() {
        authenticateWithoutUserId("buyer", buyer);
        when(buyer.getId()).thenReturn(2L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(transactionRepository.findByBuyerIdOrderByCreatedAtDesc(2L, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<MarketplaceTransactionResponse> result = transactionService.findMineAsBuyer(null, pageable);

        assertTrue(result.isEmpty());
        verify(transactionRepository).findByBuyerIdOrderByCreatedAtDesc(2L, pageable);
    }

    @Test
    void findMineAsSellerUsesStatusQuery() {
        authenticateWithoutUserId("seller", seller);
        when(seller.getId()).thenReturn(1L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(transactionRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(1L, MarketplaceTransaction.Status.COMPLETED, pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<MarketplaceTransactionResponse> result = transactionService.findMineAsSeller(MarketplaceTransaction.Status.COMPLETED, pageable);

        assertTrue(result.isEmpty());
        verify(transactionRepository).findBySellerIdAndStatusOrderByCreatedAtDesc(1L, MarketplaceTransaction.Status.COMPLETED, pageable);
    }

    @Test
    void findAllRejectsNonAdmin() {
        authenticateWithoutUserId("seller", seller);
        when(seller.getRoles()).thenReturn(Set.of());

        assertThrows(AccessDeniedException.class,
                () -> transactionService.findAll(null, PageRequest.of(0, 20)));
    }

    @Test
    void findAllAllowsAdmin() {
        authenticateWithoutUserId("admin", admin);
        when(admin.getRoles()).thenReturn(Set.of(adminRole));
        when(adminRole.getName()).thenReturn("ADMIN");
        PageRequest pageable = PageRequest.of(0, 20);
        when(transactionRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(new PageImpl<>(List.of()));

        Page<MarketplaceTransactionResponse> result = transactionService.findAll(null, pageable);

        assertTrue(result.isEmpty());
        verify(transactionRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    private void authenticateWithoutUserId(String username, User currentUser) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateAs(String username, long userId, User currentUser) {
        authenticateWithoutUserId(username, currentUser);
        when(currentUser.getId()).thenReturn(userId);
    }

    private void givenListing(UUID id, GameListing.Status status) {
        when(listingRepository.findById(id)).thenReturn(Optional.of(listing));
        when(listing.getStatus()).thenReturn(status);
    }

    private void givenAuthorizationTransaction(UUID id, long sellerId, long buyerId, MarketplaceTransaction.Status status) {
        when(transactionRepository.findWithLockById(id)).thenReturn(Optional.of(transaction));
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        lenient().when(transaction.getId()).thenReturn(id);
        when(transaction.getSeller()).thenReturn(seller);
        lenient().when(transaction.getBuyer()).thenReturn(buyer);
        when(seller.getId()).thenReturn(sellerId);
        lenient().when(buyer.getId()).thenReturn(buyerId);
        when(transaction.getStatus()).thenReturn(status);
    }

    private MarketplaceTransactionResponse givenResponseTransaction(UUID id, long sellerId, long buyerId, MarketplaceTransaction.Status status) {
        givenAuthorizationTransaction(id, sellerId, buyerId, status);
        when(transaction.getId()).thenReturn(id);
        when(transaction.getListing()).thenReturn(listing);
        when(listing.getId()).thenReturn(UUID.randomUUID());
        when(listing.getTitle()).thenReturn("Test Game");
        when(listing.getGame()).thenReturn(game);
        when(game.getId()).thenReturn("game-1");
        when(transaction.getAmount()).thenReturn(new BigDecimal("25.00"));
        when(seller.getUsername()).thenReturn("seller");
        when(buyer.getUsername()).thenReturn("buyer");
        when(transaction.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(transaction.getUpdatedAt()).thenReturn(OffsetDateTime.now());
        return MarketplaceTransactionResponse.from(transaction);
    }

    private void givenResponseFields(MarketplaceTransaction transactionSource, GameListing listingSource,
                                     User buyerSource, User sellerSource, MarketplaceTransaction.Status status) {
        when(transactionSource.getListing()).thenReturn(listingSource);
        when(transactionSource.getBuyer()).thenReturn(buyerSource);
        when(transactionSource.getSeller()).thenReturn(sellerSource);
        when(transactionSource.getStatus()).thenReturn(status);
        when(transactionSource.getAmount()).thenReturn(new BigDecimal("25.00"));
        when(transactionSource.getId()).thenReturn(UUID.randomUUID());
        when(transactionSource.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(transactionSource.getUpdatedAt()).thenReturn(OffsetDateTime.now());
        when(listingSource.getId()).thenReturn(UUID.randomUUID());
        when(listingSource.getGame()).thenReturn(game);
        when(game.getId()).thenReturn("game-1");
    }
}
