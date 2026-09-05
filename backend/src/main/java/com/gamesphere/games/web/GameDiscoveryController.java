package com.gamesphere.games.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.service.GameService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/games")
public class GameDiscoveryController {

    private final GameService gameService;

    public GameDiscoveryController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<Page<GameResponse>>> trending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Trending games retrieved", gameService.trending(pageable(page, size))));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<Page<GameResponse>>> topRated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Top-rated games retrieved", gameService.topRated(pageable(page, size))));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<Page<GameResponse>>> recent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Recently released games retrieved", gameService.recentlyReleased(pageable(page, size))));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<Page<GameResponse>>> upcoming(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming games retrieved", gameService.upcoming(pageable(page, size))));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<Page<GameResponse>>> related(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Related games retrieved", gameService.related(id, pageable(page, size))));
    }

    private Pageable pageable(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title"));
    }
}
