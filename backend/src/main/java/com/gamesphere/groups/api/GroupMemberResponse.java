package com.gamesphere.groups.api;

import com.gamesphere.groups.domain.GroupMember;

import java.time.OffsetDateTime;

public record GroupMemberResponse(
        Long userId,
        String username,
        String displayName,
        OffsetDateTime joinedAt
) {
    public static GroupMemberResponse from(GroupMember member) {
        return new GroupMemberResponse(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getDisplayName(),
                member.getJoinedAt()
        );
    }
}
