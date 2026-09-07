package com.gamesphere.progress.api;

import com.gamesphere.progress.domain.GameProgress.Status;
import com.gamesphere.progress.service.GameProgressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@PreAuthorize("isAuthenticated()")
public class GameProgressController {
    private final GameProgressService service;

    public GameProgressController(GameProgressService service) { this.service = service; }

    @PostMapping("/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    public GameProgressResponse create(@PathVariable String gameId, @Valid @RequestBody GameProgressRequest request) {
        return service.create(gameId, request);
    }

    @GetMapping
    public List<GameProgressResponse> mine(@RequestParam(required = false) Status status) {
        return service.mine(status);
    }

    @GetMapping("/games/{gameId}")
    public GameProgressResponse getByGame(@PathVariable String gameId) { return service.getByGame(gameId); }

    @PutMapping("/{id}")
    public GameProgressResponse update(@PathVariable UUID id, @Valid @RequestBody GameProgressRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }
}