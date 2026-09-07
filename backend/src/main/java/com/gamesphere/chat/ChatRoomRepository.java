package com.gamesphere.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByRoomKey(String roomKey);

    @Query("select r from ChatRoom r join ChatRoomMember m on m.room.id = r.id where m.user.id = :userId")
    Page<ChatRoom> findRoomsForUser(Long userId, Pageable pageable);
}
