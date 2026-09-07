package com.gamesphere.collections.domain;

import com.gamesphere.games.domain.Game;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "game_collection_items", uniqueConstraints = @UniqueConstraint(name = "uq_collection_items_collection_game", columnNames = {"collection_id", "game_id"}))
public class GameCollectionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private GameCollection collection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt;

    protected GameCollectionItem() {}

    public GameCollectionItem(GameCollection collection, Game game) {
        this.collection = collection;
        this.game = game;
        this.addedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public GameCollection getCollection() { return collection; }
    public Game getGame() { return game; }
    public OffsetDateTime getAddedAt() { return addedAt; }
}