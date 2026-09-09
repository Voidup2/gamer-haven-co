package com.gamesphere.forum;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forums")
public class ForumController {
    private final ForumService forumService;

    public ForumController(ForumService forumService) { this.forumService = forumService; }

    @GetMapping("/topics")
    public Page<ForumDtos.TopicResponse> topics(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String gameId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        validatePage(page, size);
        return forumService.topics(category, gameId, search,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("updatedAt"))));
    }

    @GetMapping("/topics/{topicId}")
    public ForumDtos.TopicResponse topic(@PathVariable UUID topicId) { return forumService.topic(topicId); }

    @PostMapping("/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public ForumDtos.TopicResponse createTopic(@Valid @RequestBody ForumDtos.CreateTopicRequest request) {
        return forumService.createTopic(request);
    }

    @PutMapping("/topics/{topicId}")
    public ForumDtos.TopicResponse updateTopic(@PathVariable UUID topicId, @Valid @RequestBody ForumDtos.UpdateTopicRequest request) {
        return forumService.updateTopic(topicId, request);
    }

    @DeleteMapping("/topics/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(@PathVariable UUID topicId) { forumService.deleteTopic(topicId); }

    @PutMapping("/topics/{topicId}/lock")
    public ForumDtos.TopicResponse lock(@PathVariable UUID topicId, @RequestParam boolean locked) {
        return forumService.setLock(topicId, locked);
    }

    @PutMapping("/topics/{topicId}/pin")
    public ForumDtos.TopicResponse pin(@PathVariable UUID topicId, @RequestParam boolean pinned) {
        return forumService.setPinned(topicId, pinned);
    }

    @GetMapping("/topics/{topicId}/posts")
    public Page<ForumDtos.PostResponse> posts(@PathVariable UUID topicId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "50") int size) {
        validatePage(page, size);
        return forumService.posts(topicId, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")));
    }

    @PostMapping("/topics/{topicId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ForumDtos.PostResponse createPost(@PathVariable UUID topicId, @Valid @RequestBody ForumDtos.CreatePostRequest request) {
        return forumService.createPost(topicId, request);
    }

    @PutMapping("/posts/{postId}")
    public ForumDtos.PostResponse updatePost(@PathVariable UUID postId, @Valid @RequestBody ForumDtos.UpdatePostRequest request) {
        return forumService.updatePost(postId, request);
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable UUID postId) { forumService.deletePost(postId); }

    private void validatePage(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }
}
