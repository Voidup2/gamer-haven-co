package com.gamesphere.collections.api;

import com.gamesphere.collections.service.GameCollectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
public class GameCollectionController {
    private final GameCollectionService service;

    public GameCollectionController(GameCollectionService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public GameCollectionResponse create(@Valid @RequestBody GameCollectionRequest request) { return service.create(request); }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public List<GameCollectionResponse> mine() { return service.mine(); }

    @GetMapping("/public")
    public List<GameCollectionResponse> publicCollections() { return service.publicCollections(); }

    @GetMapping("/{id}")
    public GameCollectionResponse get(@PathVariable UUID id) { return service.get(id); }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public GameCollectionResponse update(@PathVariable UUID id, @Valid @RequestBody GameCollectionRequest request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable UUID id) { service.delete(id); }

    @PostMapping("/{id}/games/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public GameCollectionItemResponse addGame(@PathVariable UUID id, @PathVariable @NotBlank String gameId) { return service.addGame(id, gameId); }

    @GetMapping("/{id}/games")
    public List<GameCollectionItemResponse> games(@PathVariable UUID id) { return service.games(id); }

    @DeleteMapping("/{id}/games/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void removeGame(@PathVariable UUID id, @PathVariable @NotBlank @Size(max = 100) String gameId) { service.removeGame(id, gameId); }
}