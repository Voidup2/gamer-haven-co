package com.gamesphere.discussions.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.discussions.api.DiscussionVoteRequest;
import com.gamesphere.discussions.api.DiscussionVoteResponse;
import com.gamesphere.discussions.domain.Discussion;
import com.gamesphere.discussions.domain.DiscussionVote;
import com.gamesphere.discussions.domain.DiscussionVote.VoteType;
import com.gamesphere.discussions.repository.DiscussionRepository;
import com.gamesphere.discussions.repository.DiscussionVoteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DiscussionVoteService {

    private final DiscussionVoteRepository voteRepository;
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;

    public DiscussionVoteService(DiscussionVoteRepository voteRepository,
                                 DiscussionRepository discussionRepository,
                                 UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.discussionRepository = discussionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DiscussionVoteResponse vote(UUID discussionId, DiscussionVoteRequest request) {
        Discussion discussion = findDiscussion(discussionId);
        User user = getCurrentUser();

        DiscussionVote vote = voteRepository
                .findByDiscussionIdAndUserId(discussionId, user.getId())
                .orElseGet(() -> new DiscussionVote(discussion, user, request.voteType()));

        vote.setVoteType(request.voteType());
        voteRepository.save(vote);

        return getSummary(discussionId, user.getId());
    }

    @Transactional
    public void removeVote(UUID discussionId) {
        findDiscussion(discussionId);
        User user = getCurrentUser();
        voteRepository.findByDiscussionIdAndUserId(discussionId, user.getId())
                .ifPresent(voteRepository::delete);
    }

    @Transactional(readOnly = true)
    public DiscussionVoteResponse getSummary(UUID discussionId) {
        findDiscussion(discussionId);
        return getSummary(discussionId, getAuthenticatedUserIdOrNull());
    }

    private DiscussionVoteResponse getSummary(UUID discussionId, Long userId) {
        long upvotes = voteRepository.countByDiscussionIdAndVoteType(
                discussionId, VoteType.UPVOTE);
        long downvotes = voteRepository.countByDiscussionIdAndVoteType(
                discussionId, VoteType.DOWNVOTE);

        VoteType currentUserVote = userId == null
                ? null
                : voteRepository.findVoteType(discussionId, userId).orElse(null);

        return new DiscussionVoteResponse(upvotes, downvotes, currentUserVote);
    }

    private Discussion findDiscussion(UUID id) {
        return discussionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long getAuthenticatedUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        return userRepository.findByUsername(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }
}
