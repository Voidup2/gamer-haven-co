package com.gamesphere.discussions.repository;

import com.gamesphere.discussions.domain.DiscussionVote;
import com.gamesphere.discussions.domain.DiscussionVote.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DiscussionVoteRepository extends JpaRepository<DiscussionVote, UUID> {

    Optional<DiscussionVote> findByDiscussionIdAndUserId(UUID discussionId, Long userId);

    long countByDiscussionIdAndVoteType(UUID discussionId, VoteType voteType);

    @Query("""
            select v.voteType
            from DiscussionVote v
            where v.discussion.id = :discussionId
              and v.user.id = :userId
            """)
    Optional<VoteType> findVoteType(
            @Param("discussionId") UUID discussionId,
            @Param("userId") Long userId
    );
}
