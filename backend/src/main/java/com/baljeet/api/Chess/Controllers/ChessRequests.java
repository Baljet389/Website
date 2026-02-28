package com.baljeet.api.Chess.Controllers;

public class ChessRequests {
    public static class StartGame{
        public String fen;
        public GameMode mode;
        public long timeLeft;
        public long increment;
        public boolean white;
    }
    public static class JoinGame{
        public String gameID;
    }
    public static class makeMove{
        public int move;
    }
    public static class engineMakeMove{
        // Time in milliseconds
        public long timeLeft;
        public long increment;
    }

}
