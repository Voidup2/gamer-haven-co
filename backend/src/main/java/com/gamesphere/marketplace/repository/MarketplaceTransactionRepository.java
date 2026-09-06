package com.gamesphere.marketplace.repository;

import com.gamesphere.marketplace.domain.MarketplaceTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.UUID;

public interface MarketplaceTransactionRepository extends JpaRepository<MarketplaceTransaction, UUID> {
    boolean existsByListingIdAndBuyerId(UUID listingId, Long buyerId);
    boolean existsByListingIdAndStatus(UUID listingId, MarketplaceTransaction.Status status);
    Page<MarketplaceTransaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    Page<MarketplaceTransaction> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    Page<MarketplaceTransaction> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, MarketplaceTransaction.Status status, Pageable pageable);
    Page<MarketplaceTransaction> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, MarketplaceTransaction.Status status, Pageable pageable);
    Page<MarketplaceTransaction> findByStatusOrderByCreatedAtDesc(MarketplaceTransaction.Status status, Pageable pageable);
    Page<MarketplaceTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<MarketplaceTransaction> findWithLockById(UUID id);
}