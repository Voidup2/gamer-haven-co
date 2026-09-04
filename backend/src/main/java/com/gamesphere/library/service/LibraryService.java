package com.gamesphere.library.service;

import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.library.domain.UserGameLibrary;
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

    public LibraryService(
            UserGameLibraryRepository libraryRepository,
            UserRepository userRepository,
            GameRepository gameRepository
    ) {
        this.libraryRepository = libraryRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public UserGameLibrary addGame(String gameId) {

        User user = getCurrentUser();

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Game not found")
                );

        boolean alreadyExists =
                libraryRepository.existsByUserIdAndGameId(
                        user.getId(),
                        gameId
                );

        if (alreadyExists) {
            throw new ConflictException(
                    "Game is already in your library"
            );
        }

        UserGameLibrary libraryEntry =
                new UserGameLibrary(user, game);

        return libraryRepository.save(libraryEntry);
    }

    @Transactional(readOnly = true)
    public List<UserGameLibrary> getLibrary() {

        User user = getCurrentUser();

        return libraryRepository.findByUserId(user.getId());
    }

    @Transactional
    public void removeGame(String gameId) {

        User user = getCurrentUser();

        boolean exists =
                libraryRepository.existsByUserIdAndGameId(
                        user.getId(),
                        gameId
                );

        if (!exists) {
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
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }
}