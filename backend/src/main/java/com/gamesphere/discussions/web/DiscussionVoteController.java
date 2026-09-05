package com.gamesphere.discussions.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.discussions.api.DiscussionVoteRequest;
import com.gamesphere.discussions.api.DiscussionVoteResponse;
import com.gamesphere.discussions.service.DiscussionVoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discussions/{discussionId}/vote")
public class DiscussionVoteController {

    private final DiscussionVoteService voteService;

    public DiscussionVoteController(DiscussionVoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DiscussionVoteResponse>> getSummary(
            @PathVariable UUID discussionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Discussion votes retrieved successfully",
                voteService.getSummary(discussionId)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DiscussionVoteResponse>> vote(
            @PathVariable UUID discussionId,
            @Valid @RequestBody DiscussionVoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Discussion vote saved successfully",
                voteService.vote(discussionId, request)));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeVote(
            @PathVariable UUID discussionId) {
        voteService.removeVote(discussionId);
        return ResponseEntity.ok(ApiResponse.success(
                "Discussion vote removed successfully", null));
    }
}
