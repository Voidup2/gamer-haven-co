package com.gamesphere.chat;

import com.gamesphere.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {
    boolean existsByRoomIdAndUserId(UUID roomId, Long userId);
    Optional<ChatRoomMember> findByRoomIdAndUserId(UUID roomId, Long userId);
    List<ChatRoomMember> findByUserId(Long userId);
    long countByRoomId(UUID roomId);
    void deleteByRoomIdAndUserId(UUID roomId, Long userId);

    @Query("select m.user from ChatRoomMember m where m.room.id = :roomId and m.user.id <> :userId")
    Optional<User> findOtherUser(@Param("roomId") UUID roomId, @Param("userId") Long userId);
}
