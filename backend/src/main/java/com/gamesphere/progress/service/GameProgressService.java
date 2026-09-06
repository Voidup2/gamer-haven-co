package com.gamesphere.progress.service;

import com.gamesphere.activity.domain.UserActivity;
import com.gamesphere.activity.service.UserActivityService;
import com.gamesphere.auth.domain.User;
import com.gamesphere.auth.repository.UserRepository;
import com.gamesphere.common.web.ConflictException;
import com.gamesphere.common.web.ResourceNotFoundException;
import com.gamesphere.games.domain.Game;
import com.gamesphere.games.repository.GameRepository;
import com.gamesphere.progress.api.GameProgressRequest;
import com.gamesphere.progress.api.GameProgressResponse;
import com.gamesphere.progress.domain.GameProgress;
import com.gamesphere.progress.repository.GameProgressRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class GameProgressService {
    private final GameProgressRepository progressRepository; private final GameRepository gameRepository; private final UserRepository userRepository; private final UserActivityService activityService;
    public GameProgressService(GameProgressRepository progressRepository, GameRepository gameRepository, UserRepository userRepository, UserActivityService activityService) { this.progressRepository=progressRepository; this.gameRepository=gameRepository; this.userRepository=userRepository; this.activityService=activityService; }
    @Transactional public GameProgressResponse create(String gameId, GameProgressRequest request) { User user=currentUser(); Game game=gameRepository.findById(gameId).orElseThrow(() -> new ResourceNotFoundException("Game not found")); if(progressRepository.existsByUserIdAndGameId(user.getId(),gameId)) throw new ConflictException("Progress already exists for this game"); validate(request); GameProgress saved=progressRepository.save(new GameProgress(user,game,request.status(),request.playtimeMinutes(),request.progressPercent(),request.notes())); recordProgressActivity(user,game,saved); return GameProgressResponse.from(saved); }
    @Transactional(readOnly=true) public List<GameProgressResponse> mine(GameProgress.Status status) { Long userId=currentUser().getId(); List<GameProgress> progress=status==null ? progressRepository.findByUserIdOrderByLastPlayedAtDesc(userId) : progressRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId,status); return progress.stream().map(GameProgressResponse::from).toList(); }
    @Transactional(readOnly=true) public GameProgressResponse getByGame(String gameId) { return GameProgressResponse.from(progressRepository.findByUserIdAndGameId(currentUser().getId(),gameId).orElseThrow(() -> new ResourceNotFoundException("Game progress not found"))); }
    @Transactional public GameProgressResponse update(UUID id, GameProgressRequest request) { GameProgress progress=owned(id); validate(request); progress.update(request.status(),request.playtimeMinutes(),request.progressPercent(),request.notes()); GameProgress saved=progressRepository.save(progress); recordProgressActivity(currentUser(),progress.getGame(),saved); return GameProgressResponse.from(saved); }
    @Transactional public void delete(UUID id) { progressRepository.delete(owned(id)); }
    private void validate(GameProgressRequest request) { if(request.status()==GameProgress.Status.COMPLETED && request.progressPercent()!=100) throw new IllegalArgumentException("Completed games must have 100% progress"); }
    private void recordProgressActivity(User user, Game game, GameProgress progress) { UserActivity.ActivityType type=progress.getStatus()==GameProgress.Status.COMPLETED ? UserActivity.ActivityType.GAME_COMPLETED : UserActivity.ActivityType.PROGRESS_UPDATED; String title=type==UserActivity.ActivityType.GAME_COMPLETED ? "Completed a game" : "Updated game progress"; activityService.record(user,type,title,game.getTitle(),"GAME",game.getId()); }
    private GameProgress owned(UUID id) { GameProgress progress=progressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Game progress not found")); if(!progress.getUser().getId().equals(currentUser().getId())) throw new AccessDeniedException("You are not allowed to modify this progress"); return progress; }
    private User currentUser() { Authentication authentication=SecurityContextHolder.getContext().getAuthentication(); if(authentication==null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) throw new AccessDeniedException("Authentication required"); return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
}