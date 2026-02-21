package com.baljeet.api.Chess.Controllers;


import java.util.ArrayList;

public class ChessResponses {
    public static class getMovesResponse{
        public ArrayList<Integer> moves;
    }
    public static class gameState{
        public String fen;
        public boolean check;
        public boolean checkmate;
        public boolean draw;

        public long blackTime;
        public long whiteTime;

        public String timeControl;
        public boolean player1Turn;
    }
}
