package com.baljeet.api.Chess.GameLayers;

import com.baljeet.api.Chess.Controllers.ChessRequests;
import com.baljeet.api.Chess.Controllers.ChessResponses;
import com.baljeet.api.Chess.Controllers.GameMode;
import com.baljeet.api.Chess.Controllers.GameResult;
import com.baljeet.api.Chess.Core.*;
import com.baljeet.api.Chess.Engine.*;
import java.util.ArrayList;
import java.util.UUID;


public class Game {
    private final Board board;
    private final MoveGeneration moveGeneration;
    private final Engine engine;
    private boolean active = false;

    private final Player player1;
    private final Player player2;
    private final long increment;
    private long lastTime;

    public final String gameID;
    private final GameMode mode;

    public Game(ChessRequests.StartGame request){
        board = new Board(request.fen);
        moveGeneration = new MoveGeneration(board);
        engine = new BaljeetEngine(board, moveGeneration);

        increment = request.increment;
        long timeLeft = request.timeLeft;

        player1 = new Player(timeLeft, request.white);
        player2 = new Player(timeLeft, !request.white);
        lastTime = System.currentTimeMillis();

        gameID = generateGameId();
        mode = request.mode;
        switch (mode){
            case LOCAL, ENGINE -> active = true;
            case ONLINE -> active = false;
        }
    }
    public ChessResponses.StartGame startGame(){
        var startResponse = new ChessResponses.StartGame();
        startResponse.gameID = gameID;
        return startResponse;
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
        if(!active)
            return null;
        updatePlayerTimes();

        var moves = moveGeneration.getAllMoves(false);
        int move = request.move;
        int count = 0;
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i) == move)
                count++;
        }
        if(count == 0)
            return null;

        board.makeMove(request.move);
        return getGameState();
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

        if (moveList.isEmpty()) {
            if (moveGeneration.check)
                state.checkmate = true;
            else
                state.draw = true;
        }
        if (moveGeneration.check)
            state.check = true;
        state.fen = board.toString();
        if(isRepetition(board.zobristHash) || board.halfMoveClock >= 100)
            state.draw = true;

        Player whitePlayer = player1.white ?
                player1 : player2;
        Player blackPlayer = player1.white ?
                player2 : player1;
        state.whiteTime = whitePlayer.timeLeft;
        state.blackTime = blackPlayer.timeLeft;

        if(state.draw)
            state.result = GameResult.DRAW;
        else if(state.checkmate && board.whiteToMove)
            state.result = GameResult.WINNER_BLACK;
        else if(state.checkmate)
            state.result = GameResult.WINNER_WHITE;
        else
            state.result = GameResult.NO_RESULT;

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
    public ChessResponses.JoinGame joinGame(){
        if(active || mode != GameMode.ONLINE) return null;
        active = true;
        lastTime = System.currentTimeMillis();

        var joinResponse = new ChessResponses.JoinGame();
        joinResponse.gameID = gameID;
        joinResponse.timeLeft = player2.timeLeft;
        joinResponse.increment = increment;
        joinResponse.white = player2.white;

        return joinResponse;
    }
    public void updatePlayerTimes(){
        Player currPlayer = (board.whiteToMove == player1.white)
                ? player1 : player2;

        long currTime = System.currentTimeMillis();
        long timeSpend = currTime - lastTime;
        currPlayer.timeLeft =
                currPlayer.timeLeft - timeSpend + increment;

        lastTime = currTime;
    }
    static class Player {
        public long timeLeft;
        public boolean white;
        Player(long timeLeft, boolean white){
            this.timeLeft = timeLeft;
            this.white = white;
        }
    }
    public static String generateGameId() {
        return "g_" + UUID.randomUUID().toString().replace("-", "");
    }
}
