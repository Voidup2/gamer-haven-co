package com.gamesphere.dashboard.api;

import com.gamesphere.dashboard.service.UserDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/dashboard")
@PreAuthorize("isAuthenticated()")
public class UserDashboardController {
    private final UserDashboardService service;

    public UserDashboardController(UserDashboardService service) {
        this.service = service;
    }

    @GetMapping
    public UserDashboardResponse mine() {
        return service.mine();
    }
}
