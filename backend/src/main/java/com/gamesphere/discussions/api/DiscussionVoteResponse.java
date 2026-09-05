package com.gamesphere.discussions.api;

import com.gamesphere.discussions.domain.DiscussionVote.VoteType;

public record DiscussionVoteResponse(
        long upvotes,
        long downvotes,
        VoteType currentUserVote
) {}
