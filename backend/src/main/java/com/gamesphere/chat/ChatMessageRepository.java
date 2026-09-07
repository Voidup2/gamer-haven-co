package com.gamesphere.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    Page<ChatMessage> findByRoomIdAndDeletedAtIsNull(UUID roomId, Pageable pageable);
    long countByRoomIdAndCreatedAtAfter(UUID roomId, java.time.OffsetDateTime timestamp);
}
