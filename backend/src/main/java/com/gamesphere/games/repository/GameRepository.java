package com.gamesphere.games.repository;

import com.gamesphere.games.domain.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, String> {

    Page<Game> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );
}
