package com.baljeet.api.Chess.Engine;

import com.baljeet.api.Chess.Core.Board;
import org.springframework.stereotype.Component;

@Component
public class EvaluationData {
    public static final int[] B_PAWN_PST = {
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0};

    public static final int[] B_KING_PST_MIDDLE = {
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            20, 20,  0,  0,  0,  0, 20, 20,
            20, 40, 10,  0,  0, 10, 40, 20};

    public static final int[] B_KING_PST_END = {
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50};

    public static final int[] KNIGHT_PST = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50,};

    public static final int[] B_BISHOP =  {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20,
    };
    public static int[] W_BISHOP ;
    public static int[] W_PAWN_PST;
    public static int[] W_KING_PST_MIDDLE;
    public static int[] W_KING_PST_END;

    public static final int QUEEN_WEIGHT = 900;
    public static final int ROOK_WEIGHT = 500;
    public static final int BISHOP_WEIGHT = 320;
    public static final int KNIGHT_WEIGHT = 300;
    public static final int PAWN_WEIGHT = 100;

    public static final int PHASE_KNIGHT = 1;
    public static final int PHASE_BISHOP = 1;
    public static final int PHASE_ROOK   = 2;
    public static final int PHASE_QUEEN  = 4;
    public static final int TOTAL_PHASE = 16;

    public static final int[] PieceWeights = {
            0,PAWN_WEIGHT,KNIGHT_WEIGHT,
            BISHOP_WEIGHT,ROOK_WEIGHT,
            QUEEN_WEIGHT,0};

    // Most Valuable Victim / Least Valuable Attacker
    public static int[][] MVVLVA = new int[7][7];
    public static int[][] Manhattan = new int[64][64];

    public static long A_FILE = 0x8080808080808080L;
    public static long[] files = new long[8];
    public static long[] isolatedPawnMask = new long[8];

    public EvaluationData(){
        W_PAWN_PST = mirrorForBlack(B_PAWN_PST);
        W_KING_PST_MIDDLE = mirrorForBlack(B_KING_PST_MIDDLE);
        W_KING_PST_END = mirrorForBlack(B_KING_PST_END);
        W_BISHOP = mirrorForBlack(B_BISHOP);
        calculateMVVLVA();
        calculateManhattanDistance();
        calculateFiles();
    }
    private void calculateMVVLVA() {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                // victim, attacker
                MVVLVA[i][j] = PieceWeights[i] - PieceWeights[j];
            }
        }
    }
    private int[] mirrorForBlack(int[] whitePST) {
        int[] blackPST = new int[64];
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            int mirroredRank = 7 - rank;
            int mirroredSquare = mirroredRank * 8 + file;

            blackPST[square] = whitePST[mirroredSquare];
        }
        return blackPST;
    }
    private void calculateManhattanDistance(){
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {
                Manhattan[i][j] = Math.abs(i % 8 - j % 8) + Math.abs(i / 8 - j / 8);

            }
        }
    }

    private void calculateFiles() {
        for (int i = 0; i < 8; i++) {
            files[i] = (A_FILE >>> i);
        }
        for (int i = 0; i < 8; i++) {
            if (i == 0) isolatedPawnMask[i] = files[i + 1];
            else if (i == 7) isolatedPawnMask[i] = files[i - 1];
            else isolatedPawnMask[i] = files[i - 1] | files[i + 1];
        }
    }
}
