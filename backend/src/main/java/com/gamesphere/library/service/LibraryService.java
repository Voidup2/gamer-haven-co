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
import com.gamesphere.library.repository.UserGameLibraryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class LibraryService {
    private final UserGameLibraryRepository libraryRepository; private final UserRepository userRepository; private final GameRepository gameRepository; private final UserActivityService activityService;
    public LibraryService(UserGameLibraryRepository libraryRepository, UserRepository userRepository, GameRepository gameRepository, UserActivityService activityService) { this.libraryRepository=libraryRepository; this.userRepository=userRepository; this.gameRepository=gameRepository; this.activityService=activityService; }
    @Transactional public UserGameLibrary addGame(String gameId) { User user=getCurrentUser(); Game game=gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found")); if(libraryRepository.existsByUserIdAndGameId(user.getId(),gameId)) throw new ConflictException("Game is already in your library"); UserGameLibrary entry=libraryRepository.save(new UserGameLibrary(user,game)); activityService.record(user,UserActivity.ActivityType.LIBRARY_ADDED,"Added game to library",game.getTitle(),"GAME",gameId); return entry; }
    @Transactional(readOnly=true) public List<UserGameLibrary> getLibrary() { return libraryRepository.findByUserId(getCurrentUser().getId()); }
    @Transactional public void removeGame(String gameId) { User user=getCurrentUser(); if(!libraryRepository.existsByUserIdAndGameId(user.getId(),gameId)) throw new ResourceNotFoundException("Game is not in your library"); libraryRepository.deleteByUserIdAndGameId(user.getId(),gameId); }
    private User getCurrentUser() { Authentication authentication=SecurityContextHolder.getContext().getAuthentication(); return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found")); }
}