package com.baljeet.api.Chess.Core;

public class Piece {
    public static final int EMPTY = 0;

    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;

    //flags for move encoding
    public static final int NO_FLAG = 0;
    //that tastes like
    public static final int PROMOTION_KNIGHT = 2;
    public static final int PROMOTION_BISHOP = 3;
    public static final int PROMOTION_ROOK = 4;
    public static final int PROMOTION_QUEEN = 5;

    public static final int DOUBLE_PUSH = 6;
    public static final int EN_PASSANT = 7;

    public static final int QUEEN_CASTLE = 8;
    public static final int KING_CASTLE = 9;

    public static final int CAPTURE = 10;


}
