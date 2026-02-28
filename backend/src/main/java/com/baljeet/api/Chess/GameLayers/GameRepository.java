package com.baljeet.api.Chess.GameLayers;

import com.baljeet.api.Chess.GameLayers.Game;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepository {
    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

    public Optional<Game> findById(final String gameID) {
        return Optional.ofNullable(games.get(gameID));
    }
    public void saveGame(final String gameID, Game game){
        games.put(gameID,game);
    }
    public void deleteGame(final String gameID){
        games.remove(gameID);
    }
}
