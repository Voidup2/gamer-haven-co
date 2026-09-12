package com.gamesphere.library.service;

import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.domain.UserGameLibrary;
import com.gamesphere.library.dto.LibraryGameResponse;
import com.gamesphere.library.repository.UserGameLibraryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibraryService {

    private final UserGameLibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserActivityService userActivityService;

    public LibraryService(
            UserGameLibraryRepository libraryRepository,
            UserRepository userRepository,
            GameRepository gameRepository,
            UserActivityService userActivityService
    ) {
        this.libraryRepository = libraryRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userActivityService = userActivityService;
    }

    @Transactional
    public LibraryGameResponse addToLibrary(String gameId) {
        User user = getCurrentUser();

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Game not found"));

        if (libraryRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new ConflictException("Game is already in your library");
        }

        UserGameLibrary libraryEntry =
                libraryRepository.save(new UserGameLibrary(user, game));

        userActivityService.record(
        user,
        UserActivity.ActivityType.LIBRARY_ADDED,
        "Added game to library",
        game.getTitle(),
        "GAME",
        gameId
);

        return new LibraryGameResponse(
                game.getId(),
                game.getTitle(),
                libraryEntry.getAddedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<LibraryGameResponse> getLibrary() {
        User user = getCurrentUser();

        return libraryRepository.findByUserId(user.getId())
                .stream()
                .map(entry -> new LibraryGameResponse(
                        entry.getGame().getId(),
                        entry.getGame().getTitle(),
                        entry.getAddedAt()
                ))
                .toList();
    }

    @Transactional
    public void removeFromLibrary(String gameId) {
        User user = getCurrentUser();

        if (!libraryRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new ResourceNotFoundException(
                    "Game is not in your library"
            );
        }

        libraryRepository.deleteByUserIdAndGameId(
                user.getId(),
                gameId
        );
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not found");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}