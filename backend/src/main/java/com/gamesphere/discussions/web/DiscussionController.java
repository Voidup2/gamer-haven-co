package com.gamesphere.discussions.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.discussions.api.DiscussionRequest;
import com.gamesphere.discussions.api.DiscussionResponse;
import com.gamesphere.discussions.service.DiscussionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class DiscussionController {

    private final DiscussionService discussionService;

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @PostMapping("/games/{gameId}/discussions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DiscussionResponse>> create(
            @PathVariable String gameId,
            @Valid @RequestBody DiscussionRequest request
    ) {
        DiscussionResponse response =
                discussionService.create(gameId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Discussion created successfully",
                        response
                ));
    }

    @GetMapping("/games/{gameId}/discussions")
    public ResponseEntity<ApiResponse<List<DiscussionResponse>>> findByGame(
            @PathVariable String gameId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Discussions retrieved successfully",
                        discussionService.findByGameId(gameId)
                )
        );
    }

    @GetMapping("/discussions/{id}")
    public ResponseEntity<ApiResponse<DiscussionResponse>> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Discussion retrieved successfully",
                        discussionService.findById(id)
                )
        );
    }

    @PutMapping("/discussions/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DiscussionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DiscussionRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Discussion updated successfully",
                        discussionService.update(id, request)
                )
        );
    }

    @DeleteMapping("/discussions/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id
    ) {
        discussionService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Discussion deleted successfully",
                        null
                )
        );
    }
}