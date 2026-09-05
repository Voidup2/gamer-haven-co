package com.gamesphere.discussions.repository;

import com.gamesphere.discussions.domain.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiscussionRepository extends JpaRepository<Discussion, UUID> {

    List<Discussion> findByGameIdOrderByCreatedAtDesc(String gameId);

    List<Discussion> findByUserIdOrderByCreatedAtDesc(Long userId);
}