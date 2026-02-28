package com.baljeet.api.Chess.GameLayers;

import com.baljeet.api.Chess.Controllers.ChessRequests;
import com.baljeet.api.Chess.Controllers.ChessResponses;
import com.baljeet.api.Chess.Core.PrecomputedData;
import com.baljeet.api.Chess.Engine.EvaluationData;
import com.baljeet.api.Chess.Engine.OpeningDatabase;
import com.baljeet.api.Chess.GameLayers.Game;
import com.baljeet.api.Chess.GameLayers.GameRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class GameService {
    private final GameRepository gameRepository;

    GameService(PrecomputedData precomputedData, OpeningDatabase openingDatabase,
                GameRepository gameRepository, EvaluationData evaluationData) {
        this.gameRepository = gameRepository;
    }

    public Optional<ChessResponses.StartGame> startGame(ChessRequests.StartGame request) {
        Game game = new Game(request);
        gameRepository.saveGame(game.gameID, game);
        return Optional.ofNullable(game.startGame());
    }

    public Optional<Game> findGame(String gameID) {
        return gameRepository.findById(gameID);
    }

    public Optional<ChessResponses.gameState> makeMove(String gameID, ChessRequests.makeMove request) {
        return gameRepository.findById(gameID)
                .map(game -> (game.makeMove(request)));

    }

    public Optional<ChessResponses.getMovesResponse> getMovesSquare(String gameID, int square) {
        return gameRepository.findById(gameID)
                .map(game -> (game.getMoves(square)));
    }

    public Optional<ChessResponses.gameState> makeEngineMove(String gameID, ChessRequests.engineMakeMove request) {
        return gameRepository.findById(gameID)
                .map(game -> (game.makeEngineMove(request.timeLeft, request.increment)));
    }

    public void deleteGame(String gameID) {
        gameRepository.deleteGame(gameID);
    }

    public Optional<ChessResponses.JoinGame> joinGame(ChessRequests.JoinGame request){
        return gameRepository.findById(request.gameID)
                .map(Game::joinGame);
    }
    public Optional<ChessResponses.gameState> getGameState(String gameID){
        return gameRepository.findById(gameID)
                .map(Game::getGameState);
    }
    public Optional<ChessResponses.gameState> checkTimeout(String gameID){
        return gameRepository.findById(gameID)
                .map(game->{
                    game.updatePlayerTimes();
                    return game.getGameState();
                });
    }
}
