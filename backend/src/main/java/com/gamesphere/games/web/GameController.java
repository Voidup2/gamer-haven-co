package com.gamesphere.games.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.games.api.GameRequest;
import com.gamesphere.games.api.GameResponse;
import com.gamesphere.games.service.GameService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
public ResponseEntity<ApiResponse<Page<GameResponse>>> findAll(
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "title") String sortBy,
        @RequestParam(defaultValue = "asc") String direction
) {

    if (page < 0) {
        throw new IllegalArgumentException(
                "Page number cannot be negative"
        );
    }

    if (size < 1 || size > 100) {
        throw new IllegalArgumentException(
                "Page size must be between 1 and 100"
        );
    }
     Set<String> allowedSortFields = Set.of(
            "title",
            "rating",
            "price",
            "releaseDate",
            "releaseYear",
            "reviewCount"
    );
    if (!allowedSortFields.contains(sortBy)) {
        throw new IllegalArgumentException(
                "Invalid sort field: " + sortBy
        );
    }

    Sort.Direction sortDirection;

    try {
        sortDirection = Sort.Direction.fromString(direction);
    } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException(
                "Direction must be 'asc' or 'desc'"
        );
    }

    Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(sortDirection, sortBy)
    );

    return ResponseEntity.ok(
            ApiResponse.success(
                    "Games retrieved",
                    gameService.findAll(
                            search,
                            pageable
                    )
            )
    );
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
