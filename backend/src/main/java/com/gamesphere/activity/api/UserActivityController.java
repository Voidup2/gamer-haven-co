package com.gamesphere.activity.api;

import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.service.UserActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/activity")
@PreAuthorize("isAuthenticated()")
public class UserActivityController {
    private final UserActivityService service;
    public UserActivityController(UserActivityService service) { this.service=service; }

    @GetMapping
    public Page<UserActivityResponse> mine(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size, @RequestParam(required=false) UserActivity.ActivityType type) {
        if(page < 0) throw new IllegalArgumentException("page must be >= 0");
        if(size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        return service.mine(type, PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"createdAt")));
    }
}