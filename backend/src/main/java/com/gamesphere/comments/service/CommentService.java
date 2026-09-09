package com.gamesphere.comments.service;

import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.comments.api.CommentRequest;
import com.gamesphere.comments.api.CommentResponse;
import com.gamesphere.comments.domain.Comment;
import com.gamesphere.comments.repository.CommentRepository;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.discussions.domain.Discussion;
import com.gamesphere.discussions.repository.DiscussionRepository;
import com.gamesphere.notifications.domain.Notification.NotificationType;
import com.gamesphere.notifications.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;
    private final UserActivityService activityService;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, DiscussionRepository discussionRepository,
                          UserRepository userRepository, UserActivityService activityService,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.discussionRepository = discussionRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.notificationService = notificationService;
    }

    @Transactional
    public CommentResponse create(UUID discussionId, CommentRequest request) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));
        User user = getCurrentUser();
        Comment comment = new Comment(discussion, user, request.content());
        CommentResponse response = CommentResponse.from(commentRepository.save(comment));
        activityService.record(user, UserActivity.ActivityType.DISCUSSION_CREATED,
                "Commented on a discussion", request.content(), "DISCUSSION", discussionId.toString());
        if (!discussion.getUser().getId().equals(user.getId())) {
            notificationService.create(discussion.getUser(), NotificationType.REPLY,
                    "New discussion comment",
                    user.getDisplayName() != null ? user.getDisplayName() + " commented on your discussion" : user.getUsername() + " commented on your discussion",
                    "DISCUSSION", discussionId.toString());
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findByDiscussionId(UUID discussionId) {
        if (!discussionRepository.existsById(discussionId)) throw new ResourceNotFoundException("Discussion not found");
        return commentRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId).stream().map(CommentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CommentResponse findById(UUID id) { return CommentResponse.from(findComment(id)); }

    @Transactional
    public CommentResponse update(UUID id, CommentRequest request) {
        Comment comment = findComment(id); User currentUser = getCurrentUser(); checkOwnerOrAdmin(comment, currentUser);
        comment.setContent(request.content()); return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void delete(UUID id) {
        Comment comment = findComment(id); User currentUser = getCurrentUser(); checkOwnerOrAdmin(comment, currentUser); commentRepository.delete(comment);
    }

    private Comment findComment(UUID id) { return commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment not found")); }
    private User getCurrentUser() { Authentication authentication=SecurityContextHolder.getContext().getAuthentication(); if(authentication==null || !authentication.isAuthenticated()) throw new AccessDeniedException("Authentication required"); return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private void checkOwnerOrAdmin(Comment comment, User currentUser) { boolean owner=comment.getUser().getId().equals(currentUser.getId()); boolean admin=currentUser.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName())); if(!owner && !admin) throw new AccessDeniedException("You are not allowed to modify this comment"); }
}