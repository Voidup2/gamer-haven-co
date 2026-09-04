package com.gamesphere.library.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.library.domain.UserGameWishlist;
import com.gamesphere.library.dto.WishlistGameResponse;
import com.gamesphere.library.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<ApiResponse<WishlistGameResponse>> addGame(
            @PathVariable String gameId
    ) {
        UserGameWishlist entry = wishlistService.addGame(gameId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Game added to wishlist",
                        toResponse(entry)
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistGameResponse>>> getWishlist() {

        List<WishlistGameResponse> response =
                wishlistService.getWishlist()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Wishlist retrieved",
                        response
                )
        );
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Void>> removeGame(
            @PathVariable String gameId
    ) {
        wishlistService.removeGame(gameId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Game removed from wishlist",
                        null
                )
        );
    }

    private WishlistGameResponse toResponse(
            UserGameWishlist entry
    ) {
        return new WishlistGameResponse(
                entry.getGame().getId(),
                entry.getGame().getTitle(),
                entry.getAddedAt()
        );
    }
}