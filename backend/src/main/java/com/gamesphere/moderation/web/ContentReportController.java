package com.gamesphere.moderation.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.moderation.api.ContentReportRequest;
import com.gamesphere.moderation.api.ContentReportResponse;
import com.gamesphere.moderation.domain.ContentReport.Status;
import com.gamesphere.moderation.service.ContentReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ContentReportController {
    private final ContentReportService reportService;

    public ContentReportController(ContentReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/discussions/{discussionId}/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContentReportResponse>> reportDiscussion(
            @PathVariable UUID discussionId,
            @Valid @RequestBody ContentReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Discussion reported successfully",
                reportService.reportDiscussion(discussionId, request)));
    }

    @PostMapping("/comments/{commentId}/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ContentReportResponse>> reportComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody ContentReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Comment reported successfully",
                reportService.reportComment(commentId, request)));
    }

    @GetMapping("/moderation/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ContentReportResponse>>> findByStatus(
            @RequestParam(defaultValue = "PENDING") Status status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reports retrieved successfully", reportService.findByStatus(status)));
    }

    @PutMapping("/moderation/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContentReportResponse>> review(
            @PathVariable UUID id,
            @RequestParam Status status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Report reviewed successfully", reportService.review(id, status)));
    }
}
