package com.gamesphere.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {
    boolean existsByRoomIdAndUserId(UUID roomId, Long userId);
    Optional<ChatRoomMember> findByRoomIdAndUserId(UUID roomId, Long userId);
    long countByRoomId(UUID roomId);
    void deleteByRoomIdAndUserId(UUID roomId, Long userId);
}
