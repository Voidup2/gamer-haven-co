package com.gamesphere.achievements.api;

import com.gamesphere.achievements.service.AchievementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AchievementController {
    private final AchievementService service;
    public AchievementController(AchievementService service) { this.service = service; }

    @GetMapping("/games/{gameId}/achievements")
    public List<AchievementResponse> list(@PathVariable String gameId) { return service.list(gameId); }

    @PostMapping("/games/{gameId}/achievements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AchievementResponse create(@PathVariable String gameId, @Valid @RequestBody AchievementRequest request) { return service.create(gameId, request); }

    @PutMapping("/achievements/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AchievementResponse update(@PathVariable UUID id, @Valid @RequestBody AchievementRequest request) { return service.update(id, request); }

    @DeleteMapping("/achievements/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) { service.delete(id); }

    @PutMapping("/achievements/{achievementId}/progress")
    @PreAuthorize("isAuthenticated()")
    public UserAchievementResponse updateProgress(@PathVariable UUID achievementId, @Valid @RequestBody AchievementProgressRequest request) { return service.updateProgress(achievementId, request); }

    @GetMapping("/users/me/achievements")
    @PreAuthorize("isAuthenticated()")
    public List<UserAchievementResponse> mine(@RequestParam(required = false) String gameId) { return service.mine(gameId); }

    @GetMapping("/games/{gameId}/achievements/stats")
    @PreAuthorize("isAuthenticated()")
    public AchievementStatsResponse stats(@PathVariable String gameId) { return service.stats(gameId); }
}