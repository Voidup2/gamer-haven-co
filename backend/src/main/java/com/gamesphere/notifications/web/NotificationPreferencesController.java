package com.gamesphere.notifications.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.notifications.api.NotificationPreferencesRequest;
import com.gamesphere.notifications.api.NotificationPreferencesResponse;
import com.gamesphere.notifications.service.NotificationPreferencesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@PreAuthorize("isAuthenticated()")
public class NotificationPreferencesController {
    private final NotificationPreferencesService service;

    public NotificationPreferencesController(NotificationPreferencesService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getMine() {
        return ResponseEntity.ok(ApiResponse.success("Notification preferences retrieved successfully", service.getMine()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> update(
            @RequestBody NotificationPreferencesRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated successfully", service.update(request)));
    }
}
