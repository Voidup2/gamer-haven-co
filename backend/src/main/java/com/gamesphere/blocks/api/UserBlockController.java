package com.gamesphere.blocks.api;

import com.gamesphere.blocks.service.UserBlockService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/blocks")
@PreAuthorize("isAuthenticated()")
public class UserBlockController {
    private final UserBlockService service;
    public UserBlockController(UserBlockService service) { this.service = service; }

    @GetMapping
    public List<UserBlockDtos.BlockResponse> mine(Authentication authentication) { return service.mine(authentication.getName()); }

    @GetMapping("/{userId}")
    public boolean blocked(Authentication authentication, @PathVariable Long userId) { return service.isBlocked(authentication.getName(), userId); }

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserBlockDtos.BlockResponse block(Authentication authentication, @PathVariable Long userId) { return service.block(authentication.getName(), userId); }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(Authentication authentication, @PathVariable Long userId) { service.unblock(authentication.getName(), userId); }
}
