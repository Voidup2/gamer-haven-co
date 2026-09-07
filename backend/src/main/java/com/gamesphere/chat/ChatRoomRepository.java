package com.gamesphere.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByRoomKey(String roomKey);
    Page<ChatRoom> findByMembersUserId(Long userId, Pageable pageable);
}
