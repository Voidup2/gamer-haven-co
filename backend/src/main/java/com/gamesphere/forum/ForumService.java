package com.gamesphere.forum;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.blocks.service.UserBlockService;
import com.gamesphere.common.exception.ResourceNotFoundException;
import com.gamesphere.notifications.domain.Notification.NotificationType;
import com.gamesphere.notifications.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ForumService {
    private final ForumTopicRepository topicRepository;
    private final ForumPostRepository postRepository;
    private final UserRepository userRepository;
    private final UserBlockService blockService;
    private final NotificationService notificationService;

    public ForumService(ForumTopicRepository topicRepository, ForumPostRepository postRepository,
                        UserRepository userRepository, UserBlockService blockService,
                        NotificationService notificationService) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.blockService = blockService;
        this.notificationService = notificationService;
    }

    public Page<ForumDtos.TopicResponse> topics(String category, String gameId, String search, Pageable pageable) {
        Page<ForumTopic> page;
        if (search != null && !search.isBlank()) page = topicRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(search.trim(), search.trim(), pageable);
        else if (category != null && !category.isBlank()) page = topicRepository.findByCategoryIgnoreCase(category.trim(), pageable);
        else if (gameId != null && !gameId.isBlank()) page = topicRepository.findByGameId(gameId.trim(), pageable);
        else page = topicRepository.findAll(pageable);
        return page.map(this::toTopicResponse);
    }

    @Transactional public ForumDtos.TopicResponse topic(UUID id) { ForumTopic topic = findTopic(id); topic.incrementViews(); topicRepository.save(topic); return toTopicResponse(topic); }

    @Transactional public ForumDtos.TopicResponse createTopic(ForumDtos.CreateTopicRequest request) {
        User user = currentUser();
        ForumTopic topic = new ForumTopic(clean(request.title()), clean(request.category()), clean(request.content()), user, cleanNullable(request.gameId()));
        return toTopicResponse(topicRepository.save(topic));
    }

    @Transactional public ForumDtos.TopicResponse updateTopic(UUID id, ForumDtos.UpdateTopicRequest request) {
        ForumTopic topic = findTopic(id); User user = currentUser(); assertOwnerOrAdmin(topic.getAuthor(), user);
        topic.update(clean(request.title()), clean(request.category()), clean(request.content()), cleanNullable(request.gameId()));
        return toTopicResponse(topicRepository.save(topic));
    }

    @Transactional public void deleteTopic(UUID id) { ForumTopic topic = findTopic(id); assertOwnerOrAdmin(topic.getAuthor(), currentUser()); topicRepository.delete(topic); }
    @Transactional public ForumDtos.TopicResponse setLock(UUID id, boolean locked) { ForumTopic topic = findTopic(id); assertAdmin(currentUser()); topic.setLocked(locked); return toTopicResponse(topicRepository.save(topic)); }
    @Transactional public ForumDtos.TopicResponse setPinned(UUID id, boolean pinned) { ForumTopic topic = findTopic(id); assertAdmin(currentUser()); topic.setPinned(pinned); return toTopicResponse(topicRepository.save(topic)); }
    public Page<ForumDtos.PostResponse> posts(UUID topicId, Pageable pageable) { findTopic(topicId); return postRepository.findByTopicId(topicId, pageable).map(this::toPostResponse); }

    @Transactional public ForumDtos.PostResponse createPost(UUID topicId, ForumDtos.CreatePostRequest request) {
        ForumTopic topic = findTopic(topicId); User user = currentUser();
        if (topic.isLocked()) throw new IllegalStateException("Topic is locked");
        if (blockService.isEitherBlocked(user.getId(), topic.getAuthor().getId())) throw new AccessDeniedException("You cannot interact with this user");
        ForumPost post = new ForumPost(topic, user, clean(request.content()));
        ForumDtos.PostResponse response = toPostResponse(postRepository.save(post));
        if (!topic.getAuthor().getId().equals(user.getId())) {
            notificationService.create(topic.getAuthor(), NotificationType.REPLY, "New forum reply",
                    user.getDisplayName() != null ? user.getDisplayName() + " replied to your topic" : user.getUsername() + " replied to your topic",
                    "FORUM_TOPIC", topicId.toString());
        }
        return response;
    }

    @Transactional public ForumDtos.PostResponse updatePost(UUID postId, ForumDtos.UpdatePostRequest request) { ForumPost post = findPost(postId); assertOwnerOrAdmin(post.getAuthor(), currentUser()); post.setContent(clean(request.content())); return toPostResponse(postRepository.save(post)); }
    @Transactional public void deletePost(UUID postId) { ForumPost post = findPost(postId); assertOwnerOrAdmin(post.getAuthor(), currentUser()); postRepository.delete(post); }

    private ForumTopic findTopic(UUID id) { return topicRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Forum topic not found: " + id)); }
    private ForumPost findPost(UUID id) { return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Forum post not found: " + id)); }
    private ForumDtos.TopicResponse toTopicResponse(ForumTopic t) { User a = t.getAuthor(); return new ForumDtos.TopicResponse(t.getId(), t.getTitle(), t.getCategory(), t.getContent(), a.getId(), a.getUsername(), a.getDisplayName(), t.getGameId(), t.isPinned(), t.isLocked(), t.getViewCount(), postRepository.countByTopicId(t.getId()), t.getCreatedAt(), t.getUpdatedAt()); }
    private ForumDtos.PostResponse toPostResponse(ForumPost p) { User a = p.getAuthor(); return new ForumDtos.PostResponse(p.getId(), p.getTopic().getId(), a.getId(), a.getUsername(), a.getDisplayName(), p.getContent(), p.getCreatedAt(), p.getUpdatedAt()); }
    private User currentUser() { Authentication a = SecurityContextHolder.getContext().getAuthentication(); if (a == null || !a.isAuthenticated()) throw new IllegalStateException("Authentication required"); return userRepository.findByUsername(a.getName()).orElseThrow(() -> new ResourceNotFoundException("Current user not found")); }
    private void assertOwnerOrAdmin(User owner, User current) { if (!owner.getId().equals(current.getId()) && !isAdmin(current)) throw new AccessDeniedException("Not allowed"); }
    private void assertAdmin(User user) { if (!isAdmin(user)) throw new AccessDeniedException("Admin access required"); }
    private boolean isAdmin(User user) { return user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()) || "ROLE_ADMIN".equalsIgnoreCase(role.getName())); }
    private String clean(String value) { return value == null ? null : value.trim(); }
    private String cleanNullable(String value) { String v = clean(value); return v == null || v.isBlank() ? null : v; }
}
