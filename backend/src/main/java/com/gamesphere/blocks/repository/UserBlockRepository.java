package com.gamesphere.blocks.repository;

import com.gamesphere.blocks.domain.UserBlock;
import com.gamesphere.blocks.domain.UserBlockId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlockId> {
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    List<UserBlock> findByBlockerIdOrderByCreatedAtDesc(Long blockerId);
    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
