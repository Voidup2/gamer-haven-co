package com.gamesphere.collections.repository;

import com.gamesphere.collections.domain.GameCollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GameCollectionItemRepository extends JpaRepository<GameCollectionItem, Long> {
    List<GameCollectionItem> findByCollectionIdOrderByAddedAtDesc(java.util.UUID collectionId);
    Optional<GameCollectionItem> findByCollectionIdAndGameId(java.util.UUID collectionId, String gameId);
    boolean existsByCollectionIdAndGameId(java.util.UUID collectionId, String gameId);
}