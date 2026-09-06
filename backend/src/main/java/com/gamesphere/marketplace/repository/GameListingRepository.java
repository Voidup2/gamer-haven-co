package com.gamesphere.marketplace.repository;

import com.gamesphere.marketplace.domain.GameListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface GameListingRepository extends JpaRepository<GameListing, UUID>, JpaSpecificationExecutor<GameListing> {
    Page<GameListing> findByStatus(GameListing.Status status, Pageable pageable);
    Page<GameListing> findBySellerId(Long sellerId, Pageable pageable);
}
