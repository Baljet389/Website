package com.baljeet.api.Chess.GameLayers;

import com.baljeet.api.Chess.GameLayers.Game;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Optional;

@Repository
public class GameRepository {
    private final HashMap<String, Game> games = new HashMap<>();

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
