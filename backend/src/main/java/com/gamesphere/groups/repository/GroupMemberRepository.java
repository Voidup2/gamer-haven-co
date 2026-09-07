package com.gamesphere.groups.repository;

import com.gamesphere.groups.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    boolean existsByGroupIdAndUserId(UUID groupId, Long userId);
    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, Long userId);
    List<GroupMember> findByGroupIdOrderByJoinedAtAsc(UUID groupId);
    List<GroupMember> findByUserIdOrderByJoinedAtDesc(Long userId);
    long countByGroupId(UUID groupId);
    void deleteByGroupIdAndUserId(UUID groupId, Long userId);
}
