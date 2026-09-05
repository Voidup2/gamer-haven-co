package com.gamesphere.games.repository;

import com.gamesphere.games.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GameRepository extends JpaRepository<Game, String>, JpaSpecificationExecutor<Game> {
}
