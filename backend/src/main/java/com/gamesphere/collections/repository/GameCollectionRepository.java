package com.gamesphere.collections.repository;

import com.gamesphere.collections.domain.GameCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameCollectionRepository extends JpaRepository<GameCollection, UUID> {
    List<GameCollection> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<GameCollection> findByPublicCollectionTrueOrderByCreatedAtDesc();
    Optional<GameCollection> findByIdAndUserId(UUID id, Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
    boolean existsByUserIdAndNameAndIdNot(Long userId, String name, UUID id);
}