package com.gamesphere.marketplace.repository;

import com.gamesphere.marketplace.domain.MarketplaceTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MarketplaceTransactionRepository extends JpaRepository<MarketplaceTransaction, UUID> {
    boolean existsByListingIdAndBuyerId(UUID listingId, Long buyerId);
    Page<MarketplaceTransaction> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    Page<MarketplaceTransaction> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    Page<MarketplaceTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}