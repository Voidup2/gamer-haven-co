package com.gamesphere.forum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {
    Page<ForumPost> findByTopicId(UUID topicId, Pageable pageable);
    long countByTopicId(UUID topicId);
}
