package com.gamesphere.discussions.api;

import com.gamesphere.discussions.domain.DiscussionVote.VoteType;
import jakarta.validation.constraints.NotNull;

public record DiscussionVoteRequest(
        @NotNull VoteType voteType
) {}
