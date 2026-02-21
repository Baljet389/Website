package com.baljeet.api.Chess.GameLayers;

import com.baljeet.api.Chess.Controllers.ChessRequests;
import com.baljeet.api.Chess.Controllers.ChessResponses;
import com.baljeet.api.Chess.Core.*;
import com.baljeet.api.Chess.Engine.*;
import java.util.ArrayList;

public class Game {
    private final Board board;
    private final MoveGeneration moveGeneration;
    private final Engine engine;
    private boolean player2 = false;

    private final String timeControl;
    private final boolean player1Turn;

    public Game(ChessRequests.StartGameRequest request){
        board = new Board(request.fen);
        moveGeneration = new MoveGeneration(board);
        engine = new BaljeetEngine(board, board.repetitionTable);
        timeControl = request.timeControl;
        player1Turn = request.player1Turn;
    }
    public ChessResponses.gameState startGame(){
        return getGameState();
    }
    public ChessResponses.getMovesResponse getMoves(int square){
        ChessResponses.getMovesResponse response = new ChessResponses.getMovesResponse();
        MoveList moveList = moveGeneration.getAllMoves(false);
        ArrayList<Integer> list = new ArrayList<>();

        for (int i = 0; i < moveList.size(); i++) {
            if (MoveList.getFrom(moveList.get(i)) == square) {
                list.add(moveList.get(i));
            }
        }
        response.moves = list;
        return response;
    }
    public ChessResponses.gameState makeMove(ChessRequests.makeMove request){
        board.makeMove(request.move);
        var gameState = getGameState();

        gameState.blackTime = request.blackTime;
        gameState.whiteTime = request.whiteTime;
        return gameState;
    }
    public ChessResponses.gameState makeEngineMove(long timeLeft,long increment){
        int move = engine.getBestMove(timeLeft,increment);
        ChessRequests.makeMove request = new ChessRequests.makeMove();
        request.move = move;
        return makeMove(request);
    }
    public ChessResponses.gameState getGameState(){
        MoveList moveList = moveGeneration.getAllMoves(false);
        var state = new ChessResponses.gameState();
        state.timeControl = timeControl;
        state.player1Turn = player1Turn;
        if (moveList.isEmpty()){
            // Checkmate
            if (moveGeneration.check) state.checkmate = true;
            // Stalemate
            else state.draw = true;
        }
        // Check
        if (moveGeneration.check) state.check = true;
        // FEN
        state.fen = board.toString();
        // Three-fold repetition and fifty move rule
        if(isRepetition(board.zobristHash) || board.halfMoveClock >= 100) state.draw = true;
        return state;
    }
    private boolean isRepetition(long currentKey){
        int count = 0;
        for (long key: board.repetitionTable) {
            if (key == currentKey) {
                count++;
                if (count == 3) return true;
            }
        }
        return false;
    }
    public ChessResponses.gameState setPlayer2Active(ChessRequests.joinRequest request){
        if(player2) return null;
        player2 = true;
        var gameState = getGameState();
        gameState.blackTime = request.blackTime;
        gameState.whiteTime = request.whiteTime;

        return gameState;
    }

}
