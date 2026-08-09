package com.gamesphere.games.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.games.api.GameRequest;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GameResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success("Games retrieved", gameService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GameResponse>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Game retrieved", gameService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GameResponse>> create(@Valid @RequestBody GameRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Game created", gameService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GameResponse>> update(
            @PathVariable String id, @Valid @RequestBody GameRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Game updated", gameService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        gameService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Game deleted", null));
    }
}
