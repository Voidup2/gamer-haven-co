package com.gamesphere.library.repository;

import com.gamesphere.library.domain.UserGameLibrary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGameLibraryRepository
        extends JpaRepository<UserGameLibrary, Long> {

    boolean existsByUserIdAndGameId(Long userId, String gameId);

    List<UserGameLibrary> findByUserId(Long userId);

    void deleteByUserIdAndGameId(Long userId, String gameId);
}