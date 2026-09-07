package com.gamesphere.mygames.api;

import com.gamesphere.games.api.GameResponse;
import com.gamesphere.progress.api.GameProgressResponse;

import java.util.List;

public record MyGameResponse(
        GameResponse game,
        boolean inLibrary,
        boolean wishlisted,
        boolean inCollection,
        List<String> collectionNames,
        GameProgressResponse progress
) {}
