package com.gamesphere.discussions.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.discussions.api.DiscussionRequest;
import com.gamesphere.discussions.api.DiscussionResponse;
import com.gamesphere.discussions.domain.Discussion;
import com.gamesphere.discussions.repository.DiscussionRepository;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public DiscussionService(
            DiscussionRepository discussionRepository,
            GameRepository gameRepository,
            UserRepository userRepository
    ) {
        this.discussionRepository = discussionRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DiscussionResponse create(
            String gameId,
            DiscussionRequest request
    ) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Game not found"));

        User user = getCurrentUser();

        Discussion discussion = new Discussion(
                game,
                user,
                request.title(),
                request.content()
        );

        return DiscussionResponse.from(
                discussionRepository.save(discussion)
        );
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> findByGameId(String gameId) {

        if (!gameRepository.existsById(gameId)) {
            throw new ResourceNotFoundException("Game not found");
        }

        return discussionRepository
                .findByGameIdOrderByCreatedAtDesc(gameId)
                .stream()
                .map(DiscussionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DiscussionResponse findById(UUID id) {
        return DiscussionResponse.from(findDiscussion(id));
    }

    @Transactional
    public DiscussionResponse update(
            UUID id,
            DiscussionRequest request
    ) {
        Discussion discussion = findDiscussion(id);

        User currentUser = getCurrentUser();

        checkOwnerOrAdmin(discussion, currentUser);

        discussion.setTitle(request.title());
        discussion.setContent(request.content());

        return DiscussionResponse.from(
                discussionRepository.save(discussion)
        );
    }

    @Transactional
    public void delete(UUID id) {
        Discussion discussion = findDiscussion(id);

        User currentUser = getCurrentUser();

        checkOwnerOrAdmin(discussion, currentUser);

        discussionRepository.delete(discussion);
    }

    private Discussion findDiscussion(UUID id) {
        return discussionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Discussion not found"));
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication required"
            );
        }

        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private void checkOwnerOrAdmin(
            Discussion discussion,
            User currentUser
    ) {
        boolean owner = discussion.getUser()
                .getId()
                .equals(currentUser.getId());

        boolean admin = currentUser.getRoles()
                .stream()
                .anyMatch(role ->
                        "ADMIN".equals(role.getName()));

        if (!owner && !admin) {
            throw new AccessDeniedException(
                    "You are not allowed to modify this discussion"
            );
        }
    }
}