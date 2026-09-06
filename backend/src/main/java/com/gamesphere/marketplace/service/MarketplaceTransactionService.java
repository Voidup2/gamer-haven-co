package com.gamesphere.marketplace.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.marketplace.api.MarketplaceTransactionResponse;
import com.gamesphere.marketplace.domain.GameListing;
import com.gamesphere.marketplace.domain.MarketplaceTransaction;
import com.gamesphere.marketplace.repository.GameListingRepository;
import com.gamesphere.marketplace.repository.MarketplaceTransactionRepository;
import com.gamesphere.notifications.domain.Notification.NotificationType;
import com.gamesphere.notifications.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MarketplaceTransactionService {
    private final MarketplaceTransactionRepository transactionRepository;
    private final GameListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MarketplaceTransactionService(MarketplaceTransactionRepository transactionRepository,
                                          GameListingRepository listingRepository,
                                          UserRepository userRepository,
                                          NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public MarketplaceTransactionResponse initiate(UUID listingId) {
        User buyer = currentUser();
        GameListing listing = findListing(listingId);
        if (listing.getStatus() != GameListing.Status.ACTIVE) {
            throw new ConflictException("Only active listings can be purchased");
        }
        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new ConflictException("You cannot purchase your own listing");
        }
        if (transactionRepository.existsByListingIdAndBuyerId(listingId, buyer.getId())) {
            throw new ConflictException("You already have a transaction for this listing");
        }
        MarketplaceTransaction transaction = transactionRepository.save(
                new MarketplaceTransaction(listing, buyer, listing.getSeller(), listing.getPrice()));
        notificationService.create(listing.getSeller(), NotificationType.MARKETPLACE,
                "New purchase request", buyer.getUsername() + " wants to purchase your listing: " + listing.getTitle(),
                "MARKETPLACE_TRANSACTION", transaction.getId().toString());
        return MarketplaceTransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public Page<MarketplaceTransactionResponse> findMineAsBuyer(Pageable pageable) {
        return transactionRepository.findByBuyerIdOrderByCreatedAtDesc(currentUser().getId(), pageable)
                .map(MarketplaceTransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<MarketplaceTransactionResponse> findMineAsSeller(Pageable pageable) {
        return transactionRepository.findBySellerIdOrderByCreatedAtDesc(currentUser().getId(), pageable)
                .map(MarketplaceTransactionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<MarketplaceTransactionResponse> findAll(Pageable pageable) {
        requireAdmin();
        return transactionRepository.findAllByOrderByCreatedAtDesc(pageable).map(MarketplaceTransactionResponse::from);
    }

    @Transactional
    public MarketplaceTransactionResponse complete(UUID id) {
        MarketplaceTransaction transaction = findTransaction(id);
        User actor = currentUser();
        requireSellerOrAdmin(transaction, actor);
        if (transaction.getStatus() != MarketplaceTransaction.Status.PENDING) {
            throw new ConflictException("Only pending transactions can be completed");
        }
        GameListing listing = transaction.getListing();
        if (listing.getStatus() != GameListing.Status.ACTIVE) {
            throw new ConflictException("The listing is no longer active");
        }
        transaction.setStatus(MarketplaceTransaction.Status.COMPLETED);
        listing.setStatus(GameListing.Status.SOLD);
        listingRepository.save(listing);
        notificationService.create(transaction.getBuyer(), NotificationType.MARKETPLACE,
                "Purchase completed", "Your purchase of " + listing.getTitle() + " was completed.",
                "MARKETPLACE_TRANSACTION", id.toString());
        return MarketplaceTransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public MarketplaceTransactionResponse cancel(UUID id) {
        MarketplaceTransaction transaction = findTransaction(id);
        User actor = currentUser();
        if (!transaction.getBuyer().getId().equals(actor.getId()) && !transaction.getSeller().getId().equals(actor.getId()) && !isAdmin(actor)) {
            throw new AccessDeniedException("You are not allowed to cancel this transaction");
        }
        if (transaction.getStatus() != MarketplaceTransaction.Status.PENDING) {
            throw new ConflictException("Only pending transactions can be cancelled");
        }
        transaction.setStatus(MarketplaceTransaction.Status.CANCELLED);
        User recipient = transaction.getBuyer().getId().equals(actor.getId()) ? transaction.getSeller() : transaction.getBuyer();
        notificationService.create(recipient, NotificationType.MARKETPLACE,
                "Transaction cancelled", "The transaction for " + transaction.getListing().getTitle() + " was cancelled.",
                "MARKETPLACE_TRANSACTION", id.toString());
        return MarketplaceTransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public MarketplaceTransactionResponse findById(UUID id) {
        MarketplaceTransaction transaction = findTransaction(id);
        User actor = currentUser();
        if (!transaction.getBuyer().getId().equals(actor.getId()) && !transaction.getSeller().getId().equals(actor.getId()) && !isAdmin(actor)) {
            throw new AccessDeniedException("You are not allowed to access this transaction");
        }
        return MarketplaceTransactionResponse.from(transaction);
    }

    private MarketplaceTransaction findTransaction(UUID id) {
        return transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private GameListing findListing(UUID id) {
        return listingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Game listing not found"));
    }

    private void requireSellerOrAdmin(MarketplaceTransaction transaction, User actor) {
        if (!transaction.getSeller().getId().equals(actor.getId()) && !isAdmin(actor)) {
            throw new AccessDeniedException("Only the seller or an admin can complete this transaction");
        }
    }

    private void requireAdmin() {
        if (!isAdmin(currentUser())) throw new AccessDeniedException("Admin access required");
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()));
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}