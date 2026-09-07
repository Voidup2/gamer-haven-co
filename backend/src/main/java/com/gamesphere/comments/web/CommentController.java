package com.gamesphere.comments.web;

import com.gamesphere.comments.api.CommentRequest;
import com.gamesphere.comments.api.CommentResponse;
import com.gamesphere.comments.service.CommentService;
import com.gamesphere.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/discussions/{discussionId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable UUID discussionId,
            @Valid @RequestBody CommentRequest request
    ) {

        CommentResponse response =
                commentService.create(
                        discussionId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Comment created successfully",
                                response
                        )
                );
    }

    @GetMapping("/discussions/{discussionId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> findByDiscussion(
            @PathVariable UUID discussionId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comments retrieved successfully",
                        commentService.findByDiscussionId(
                                discussionId
                        )
                )
        );
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> findById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment retrieved successfully",
                        commentService.findById(id)
                )
        );
    }

    @PutMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CommentRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment updated successfully",
                        commentService.update(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id
    ) {

        commentService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comment deleted successfully",
                        null
                )
        );
    }
}