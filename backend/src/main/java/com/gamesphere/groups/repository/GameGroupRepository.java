package com.gamesphere.groups.repository;

import com.gamesphere.groups.domain.GameGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameGroupRepository extends JpaRepository<GameGroup, UUID> {
    Page<GameGroup> findByPublicGroupTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<GameGroup> findByPublicGroupTrueAndNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);
    Page<GameGroup> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);
    Optional<GameGroup> findByIdAndOwnerId(UUID id, Long ownerId);
    boolean existsByOwnerIdAndName(Long ownerId, String name);
    boolean existsByOwnerIdAndNameAndIdNot(Long ownerId, String name, UUID id);
}
