package com.gamesphere.library.web;

import com.gamesphere.common.api.ApiResponse;
import com.gamesphere.library.domain.UserGameLibrary;
import com.gamesphere.library.dto.LibraryGameResponse;
import com.gamesphere.library.service.LibraryService;
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
@RequestMapping("/api/v1/library")
@PreAuthorize("isAuthenticated()")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<ApiResponse<LibraryGameResponse>> addGame(
            @PathVariable String gameId
    ) {
        UserGameLibrary entry = libraryService.addGame(gameId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Game added to library",
                        toResponse(entry)
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LibraryGameResponse>>> getLibrary() {

        List<LibraryGameResponse> response =
                libraryService.getLibrary()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Library retrieved",
                        response
                )
        );
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Void>> removeGame(
            @PathVariable String gameId
    ) {
        libraryService.removeGame(gameId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Game removed from library",
                        null
                )
        );
    }

    private LibraryGameResponse toResponse(
            UserGameLibrary entry
    ) {
        return new LibraryGameResponse(
                entry.getGame().getId(),
                entry.getGame().getTitle(),
                entry.getAddedAt()
        );
    }
}