package com.gamesphere.comments.repository;

import com.gamesphere.comments.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByDiscussionIdOrderByCreatedAtAsc(UUID discussionId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);
}