package com.baljeet.api.Chess.Controllers;

public class ChessRequests {
    public static class makeMove{
        public int move;
        public long whiteTime;
        public long blackTime;
    }
    public static class engineMakeMove{
        //Time in milliseconds
        public long timeLeft;
        public long increment;
    }
	public static class joinRequest{
		public long whiteTime;
        public long blackTime;
	}
    public static class StartGameRequest{
        public String fen;
        public String timeControl;
        public boolean player1Turn;
    }
}
