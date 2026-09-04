package com.gamesphere.library.domain;

import com.gamesphere.auth.domain.User;
import com.gamesphere.games.domain.Game;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "user_game_library",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_library_user_game",
                        columnNames = {"user_id", "game_id"}
                )
        }
)
public class UserGameLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "game_id",
            nullable = false
    )
    private Game game;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt;

    protected UserGameLibrary() {
    }

    public UserGameLibrary(User user, Game game) {
        this.user = user;
        this.game = game;
        this.addedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Game getGame() {
        return game;
    }

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }
}
