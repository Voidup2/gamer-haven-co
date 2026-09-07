package com.gamesphere.library.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.library.domain.UserGameFavorite;
import com.gamesphere.library.dto.FavoriteGameResponse;
import com.gamesphere.library.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<ApiResponse<FavoriteGameResponse>> addGame(@PathVariable String gameId) {
        UserGameFavorite entry = favoriteService.addGame(gameId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Game added to favorites", toResponse(entry)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteGameResponse>>> getFavorites() {
        List<FavoriteGameResponse> response = favoriteService.getFavorites()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Favorites retrieved", response));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(@PathVariable String gameId) {
        return ResponseEntity.ok(ApiResponse.success("Favorite status retrieved", favoriteService.isFavorite(gameId)));
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Void>> removeGame(@PathVariable String gameId) {
        favoriteService.removeGame(gameId);
        return ResponseEntity.ok(ApiResponse.success("Game removed from favorites", null));
    }

    private FavoriteGameResponse toResponse(UserGameFavorite entry) {
        return new FavoriteGameResponse(
                entry.getGame().getId(),
                entry.getGame().getTitle(),
                entry.getGame().getCoverUrl(),
                entry.getAddedAt()
        );
    }
}
