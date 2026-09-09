package com.gamesphere.forum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ForumTopicRepository extends JpaRepository<ForumTopic, UUID> {
    Page<ForumTopic> findByCategoryIgnoreCase(String category, Pageable pageable);
    Page<ForumTopic> findByGameId(String gameId, Pageable pageable);
    Page<ForumTopic> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);
}
