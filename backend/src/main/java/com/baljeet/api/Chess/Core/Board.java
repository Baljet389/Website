package com.baljeet.api.Chess.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Board {
    public long[] whiteBitboards;
    public long[] blackBitboards;

    public int[] currentPosition;
    public long[][] zobristRandomNumbers;
    public long zobristHash;

    public boolean whiteToMove;
    public byte castlingRights;

    public int enPassantSquare = -1; // -1 if no square
    public int halfMoveClock;
    public int fullMoveNumber;

    public Stack<GameState> gameStates;
    public final ArrayList<Long> repetitionTable;


    static final byte[] CASTLING_MASK = new byte[64];


     public static class GameState {
        public int capturedPiece;
        public int halfMoveClock;
        public int enPassantSquare;
        public byte castlingRights;
        public long zobristHash;
    }
    static {
        Arrays.fill(CASTLING_MASK, (byte) 0b1111); // default: preserve all

        // White king e1 (square 4)
        CASTLING_MASK[3] &= 0b1100;
        // White rooks
        CASTLING_MASK[0] &= 0b1110; // h1: disable WK
        CASTLING_MASK[7] &= 0b1101; // a1: disable WQ
        // Black king e8 (square 60)
        CASTLING_MASK[59] &= 0b0011;
        // Black rooks
        CASTLING_MASK[56] &= 0b1011; // h8: disable BK
        CASTLING_MASK[63] &= 0b0111; // a8: disable BQ
    }

    public Board(String FEN){
         gameStates = new Stack<>();
         setBoard(FEN);
         initializeZobristHash();
         repetitionTable = new ArrayList<>(100);
         repetitionTable.add(zobristHash);
    }
    private void initializeZobristHash(){
        MersenneTwister mersenneTwister = new MersenneTwister(8);
        zobristRandomNumbers = new long[67][];

        //pieces on 64 squares
        for (int i = 0; i < 64; i++) {
            long[] random = new long[13];
            for (int j = 0; j < 13; j++) {
                random[j] = mersenneTwister.nextLong();
            }
            zobristRandomNumbers[i] = random;
        }
        //white to move
        zobristRandomNumbers[64] = new long[]{mersenneTwister.nextLong()};

        //castling
        long[] randomCastle = new long[16];
        for (int i = 0; i < 16; i++) {
            randomCastle[i] = mersenneTwister.nextLong();
        }
        zobristRandomNumbers[65] = randomCastle;

        //en passant
        long[] randomEnPassant = new long[8];
        for (int i = 0; i < 8; i++) {
            randomEnPassant[i] = mersenneTwister.nextLong();
        }
        zobristRandomNumbers[66] = randomEnPassant;

        for (int i = 0; i < 64; i++) {
            if(currentPosition[i] == 0) continue;
            int offset = ((1L << i) & whiteBitboards[currentPosition[i]]) != 0 ? 0 : 6;
            zobristHash ^= zobristRandomNumbers[i][(currentPosition[i] + offset)];
        }
        if(!whiteToMove) zobristHash ^= zobristRandomNumbers[64][0];


        zobristHash ^= zobristRandomNumbers[65][castlingRights];

        if(enPassantSquare != -1) zobristHash^= zobristRandomNumbers[66][enPassantSquare % 8];

    }
    private void setBoard(String FEN) {
        whiteBitboards = new long[7];
        blackBitboards = new long[7];

        currentPosition = new int[64];
        String[] parts = FEN.split(" ");
        String[] rows = parts[0].split("/");

        int squareIndex = 63; // a8 (top-left)
        //Board
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                if (Character.isDigit(c)) {
                    squareIndex -= (c - '0');
                } else {
                    long bit = 1L << squareIndex;
                    switch (c) {
                        //white
                        case 'P' -> {
                            whiteBitboards[1] |= bit;
                            currentPosition[squareIndex] = Piece.PAWN;
                        }
                        case 'N' -> {
                            whiteBitboards[2] |= bit;
                            currentPosition[squareIndex] = Piece.KNIGHT;
                        }
                        case 'B' -> {
                            whiteBitboards[3] |= bit;
                            currentPosition[squareIndex] = Piece.BISHOP;
                        }
                        case 'R' -> {
                            whiteBitboards[4] |= bit;
                            currentPosition[squareIndex] = Piece.ROOK;
                        }
                        case 'Q' -> {
                            whiteBitboards[5] |= bit;
                            currentPosition[squareIndex] = Piece.QUEEN;
                        }
                        case 'K' -> {
                            whiteBitboards[6] |= bit;
                            currentPosition[squareIndex] = Piece.KING;
                            //black
                        }
                        case 'p' -> {
                            blackBitboards[1] |= bit;
                            currentPosition[squareIndex] = Piece.PAWN;
                        }
                        case 'n' -> {
                            blackBitboards[2] |= bit;
                            currentPosition[squareIndex] = Piece.KNIGHT;
                        }
                        case 'b' -> {
                            blackBitboards[3] |= bit;
                            currentPosition[squareIndex] = Piece.BISHOP;
                        }
                        case 'r' -> {
                            blackBitboards[4] |= bit;
                            currentPosition[squareIndex] = Piece.ROOK;
                        }
                        case 'q' -> {
                            blackBitboards[5] |= bit;
                            currentPosition[squareIndex] = Piece.QUEEN;
                        }
                        case 'k' -> {
                            blackBitboards[6] |= bit;
                            currentPosition[squareIndex] = Piece.KING;
                        }
                    }
                    squareIndex--;
                }
            }
        }

        //turn
        whiteToMove = parts[1].equals("w");

        // 3. Castling
        String castling = parts[2];
        castlingRights |= (byte) (castling.contains("K")? 0b0001:0b0000);
        castlingRights |= (byte) (castling.contains("Q")? 0b0010:0b0000);
        castlingRights |= (byte) (castling.contains("k")? 0b0100:0b0000);
        castlingRights |= (byte) (castling.contains("q")? 0b1000:0b0000);

        // 4. En passant
        String enPassant = parts[3];
        enPassantSquare = enPassant.equals("-") ? -1 : squareToIndex(enPassant);

        // 5. Half move and Full move
        halfMoveClock = Integer.parseInt(parts[4]);
        fullMoveNumber = Integer.parseInt(parts[5]);

    }
    public void makeMove(int move){
        GameState info = new GameState();

        int from = MoveList.getFrom(move);
        int to = MoveList.getTo(move);

        int piece = currentPosition[from];
        int flag = MoveList.getFlag(move);

        long[] enemyPieces = whiteToMove ? blackBitboards : whiteBitboards;
        long[] friendlyPieces = whiteToMove ? whiteBitboards : blackBitboards;
        int friendlyOffset = whiteToMove ? 0 : 6;
        int enemyOffset = whiteToMove ? 6 : 0;

        info.enPassantSquare = enPassantSquare;
        info.castlingRights = castlingRights;
        info.capturedPiece = Piece.EMPTY;
        info.zobristHash = zobristHash;

        // Remove piece from from-square
        friendlyPieces[piece] &= ~(1L << from);
        currentPosition[from] = Piece.EMPTY;
        zobristHash ^= zobristRandomNumbers[from][piece + friendlyOffset];

        // Handle en passant capture
        if (flag == Piece.EN_PASSANT) {
            int capturedPawnSquare = whiteToMove ? to - 8 : to + 8;
            enemyPieces[Piece.PAWN] &= ~(1L << capturedPawnSquare);
            currentPosition[capturedPawnSquare] = Piece.EMPTY;
            info.capturedPiece = Piece.PAWN;

            zobristHash ^= zobristRandomNumbers[capturedPawnSquare][Piece.PAWN + enemyOffset];
        }

        // Handle regular captures
        else if (flag == Piece.CAPTURE) {
            info.capturedPiece = currentPosition[to];
            zobristHash ^= zobristRandomNumbers[to][currentPosition[to] + enemyOffset];
            enemyPieces[currentPosition[to]] &= ~(1L << to);

        }//Handle Castling
        else if (flag == Piece.KING_CASTLE) {
            currentPosition[to + 1] = Piece.ROOK;
            currentPosition[to - 1] = Piece.EMPTY;

            friendlyPieces[Piece.ROOK] |= (1L << (to + 1));
            friendlyPieces[Piece.ROOK] &= ~(1L << (to - 1));

            zobristHash ^= zobristRandomNumbers[to + 1][Piece.ROOK + friendlyOffset];
            zobristHash ^= zobristRandomNumbers[to - 1][Piece.ROOK + friendlyOffset];
        }
        else if (flag == Piece.QUEEN_CASTLE) {
            currentPosition[to - 1] = Piece.ROOK;
            currentPosition[to + 2] = Piece.EMPTY;

            friendlyPieces[Piece.ROOK] |= (1L << (to - 1));
            friendlyPieces[Piece.ROOK] &= ~(1L << (to + 2));

            zobristHash ^= zobristRandomNumbers[to - 1][Piece.ROOK + friendlyOffset];
            zobristHash ^= zobristRandomNumbers[to + 2][Piece.ROOK + friendlyOffset];
        }

        // Place moved piece
        if (flag >= Piece.PROMOTION_KNIGHT && flag <= Piece.PROMOTION_QUEEN) {
            if (currentPosition[to] != 0) {
                info.capturedPiece = currentPosition[to];
                zobristHash ^= zobristRandomNumbers[to][currentPosition[to] + enemyOffset];
                enemyPieces[currentPosition[to]] &= ~(1L << to);
            }
            friendlyPieces[flag] |= (1L << to);
            currentPosition[to] = flag;
            zobristHash ^= zobristRandomNumbers[to][flag + friendlyOffset];
        } else {
            friendlyPieces[piece] |= (1L << to);
            currentPosition[to]= piece;
            zobristHash ^= zobristRandomNumbers[to][piece+friendlyOffset];
        }
        if(info.enPassantSquare != -1) zobristHash ^= zobristRandomNumbers[66][enPassantSquare%8];
        // Update en passant square
        if (flag == Piece.DOUBLE_PUSH) {
            enPassantSquare = whiteToMove ? from + 8 : from - 8;
            zobristHash ^= zobristRandomNumbers[66][enPassantSquare % 8];
        } else {
            enPassantSquare = -1;
        }
        // Update castling rights
        zobristHash ^= zobristRandomNumbers[65][castlingRights];
        castlingRights &= CASTLING_MASK[from];
        castlingRights &= CASTLING_MASK[to];
        zobristHash ^= zobristRandomNumbers[65][castlingRights];

        // 50 move rule
        info.halfMoveClock = halfMoveClock;
        if (piece == Piece.PAWN || flag == Piece.CAPTURE) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        //move counter
        if(!whiteToMove){
            fullMoveNumber++;
        }
        whiteToMove = !whiteToMove;
        zobristHash ^= zobristRandomNumbers[64][0];
        gameStates.push(info);
        repetitionTable.add(zobristHash);
    }

    public void undoMove(int move) {
        GameState info = gameStates.pop();
        repetitionTable.remove(repetitionTable.size() - 1);

        int from = MoveList.getFrom(move);
        int to = MoveList.getTo(move);
        int flag = MoveList.getFlag(move);

        // Switch turn back
        whiteToMove = !whiteToMove;

        long[] enemyPieces = whiteToMove ? blackBitboards : whiteBitboards;
        long[] friendlyPieces = whiteToMove ? whiteBitboards : blackBitboards;

        int movedPiece = currentPosition[to];

        // Undo promotion
        if (flag >= Piece.PROMOTION_KNIGHT && flag <= Piece.PROMOTION_QUEEN) {
            // Remove promoted piece
            friendlyPieces[movedPiece] &= ~(1L << to);
            // Add pawn back
            friendlyPieces[Piece.PAWN] |= (1L << from);
            currentPosition[from] = Piece.PAWN;
            currentPosition[to] = info.capturedPiece;

            if (info.capturedPiece != Piece.EMPTY) {
                enemyPieces[info.capturedPiece] |= (1L << to);
            }
        }
        // Undo castling
        else if (flag == Piece.KING_CASTLE) {
            // Move king back
            friendlyPieces[Piece.KING] &= ~(1L << to);
            friendlyPieces[Piece.KING] |= (1L << from);
            currentPosition[from] = Piece.KING;
            currentPosition[to] = Piece.EMPTY;

            // Move rook back
            friendlyPieces[Piece.ROOK] &= ~(1L << (to + 1));
            friendlyPieces[Piece.ROOK] |= (1L << (to - 1));
            currentPosition[to - 1] = Piece.ROOK;
            currentPosition[to + 1] = Piece.EMPTY;
        } else if (flag == Piece.QUEEN_CASTLE) {
            friendlyPieces[Piece.KING] &= ~(1L << to);
            friendlyPieces[Piece.KING] |= (1L << from);
            currentPosition[from] = Piece.KING;
            currentPosition[to] = Piece.EMPTY;

            friendlyPieces[Piece.ROOK] &= ~(1L << (to - 1));
            friendlyPieces[Piece.ROOK] |= (1L << (to + 2));
            currentPosition[to + 2] = Piece.ROOK;
            currentPosition[to - 1] = Piece.EMPTY;
        }
        // Undo en passant
        else if (flag == Piece.EN_PASSANT) {
            friendlyPieces[Piece.PAWN] &= ~(1L << to);
            friendlyPieces[Piece.PAWN] |= (1L << from);
            currentPosition[from] = Piece.PAWN;
            currentPosition[to] = Piece.EMPTY;

            int capturedPawnSquare = whiteToMove ? to - 8 : to + 8;
            enemyPieces[Piece.PAWN] |= (1L << capturedPawnSquare);
            currentPosition[capturedPawnSquare] = Piece.PAWN;
        }
        // Undo normal move or capture
        else {
            friendlyPieces[movedPiece] &= ~(1L << to);
            friendlyPieces[movedPiece] |= (1L << from);
            currentPosition[from] = movedPiece;

            if (info.capturedPiece != Piece.EMPTY) {
                enemyPieces[info.capturedPiece] |= (1L << to);
                currentPosition[to] = info.capturedPiece;
            } else {
                currentPosition[to] = Piece.EMPTY;
            }
        }

        // Restore game state
        enPassantSquare = info.enPassantSquare;
        castlingRights = info.castlingRights;
        halfMoveClock = info.halfMoveClock;
        zobristHash = info.zobristHash;

        if (!whiteToMove) {
            fullMoveNumber--;
        }
    }

    public static int squareToIndex(String square) {
        int file = 'h' - square.charAt(0);
        int rank = square.charAt(1) - '1';
        return rank * 8 + file;
    }
    public static String indexToSquare(int index){
        int rank = index / 8;
        int file = index % 8;
        char fileChar = (char) ('h' - file);
        char rankChar = (char) ('1' + rank);
        return "" + fileChar + rankChar;
    }
    @Override
    public String toString() {
        StringBuilder fen = new StringBuilder();

        // 1. Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 7; file >=0 ; file--) {
                int square = rank * 8 + file;
                char pieceChar = getPieceChar(square);
                if (pieceChar == '.') {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceChar);
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (rank > 0) fen.append('/');
        }

        // 2. Turn
        fen.append(whiteToMove ? " w " : " b ");

        // 3. Castling rights
        StringBuilder castling = new StringBuilder();
        if ((castlingRights & 0b0001) != 0) castling.append("K");
        if ((castlingRights & 0b0010) != 0) castling.append("Q");
        if ((castlingRights & 0b0100) != 0) castling.append("k");
        if ((castlingRights & 0b1000) != 0) castling.append("q");
        fen.append(!castling.isEmpty() ? castling : "-").append(" ");

        // 4. En passant
        fen.append(enPassantSquare == -1 ? "-" : indexToSquare(enPassantSquare)).append(" ");

        // 5. Half move and full move
        fen.append(halfMoveClock).append(" ").append(fullMoveNumber);

        return fen.toString();
    }
    private char getPieceChar(int square) {
        long bit = 1L << square;

        // White
        if ((whiteBitboards[Piece.PAWN] & bit) != 0) return 'P';
        if ((whiteBitboards[Piece.KNIGHT] & bit) != 0) return 'N';
        if ((whiteBitboards[Piece.BISHOP] & bit) != 0) return 'B';
        if ((whiteBitboards[Piece.ROOK] & bit) != 0) return 'R';
        if ((whiteBitboards[Piece.QUEEN] & bit) != 0) return 'Q';
        if ((whiteBitboards[Piece.KING] & bit) != 0) return 'K';

        // Black
        if ((blackBitboards[Piece.PAWN] & bit) != 0) return 'p';
        if ((blackBitboards[Piece.KNIGHT] & bit) != 0) return 'n';
        if ((blackBitboards[Piece.BISHOP] & bit) != 0) return 'b';
        if ((blackBitboards[Piece.ROOK] & bit) != 0) return 'r';
        if ((blackBitboards[Piece.QUEEN] & bit) != 0) return 'q';
        if ((blackBitboards[Piece.KING] & bit) != 0) return 'k';

        return '.';
    }

    public boolean isInCheck() {
        long[] enemy = whiteToMove ? blackBitboards : whiteBitboards;
        long[] friendly = whiteToMove ? whiteBitboards : blackBitboards;

        long occupied = friendly[Piece.PAWN] | friendly[Piece.KNIGHT] |
                        friendly[Piece.BISHOP] | friendly[Piece.ROOK] |
                        friendly[Piece.QUEEN] | friendly[Piece.KING] |
                        enemy[Piece.PAWN] | enemy[Piece.KNIGHT] |
                        enemy[Piece.BISHOP] | enemy[Piece.ROOK] |
                        enemy[Piece.QUEEN] | enemy[Piece.KING];

        long king = friendly[Piece.KING];
        if (king == 0) return false;
        int kingSq = Long.numberOfTrailingZeros(king);

        long pawnAttackers = whiteToMove
                ? PrecomputedData.whitePawnAttacks[kingSq]
                : PrecomputedData.blackPawnAttacks[kingSq];

        if ((enemy[Piece.PAWN] & pawnAttackers) != 0) return true;
        if ((enemy[Piece.KNIGHT] & PrecomputedData.knightMoves[kingSq]) != 0) return true;

        long bishopAttacks = PrecomputedData.getBishopAttacks(kingSq, occupied);
        if ((bishopAttacks & (enemy[Piece.BISHOP] | enemy[Piece.QUEEN])) != 0) return true;

        long rookAttacks = PrecomputedData.getRookAttacks(kingSq, occupied);
        return (rookAttacks & (enemy[Piece.ROOK] | enemy[Piece.QUEEN])) != 0;

    }
    public static void printBoard(long bitboard) {
        long mask = 1L << 63;

        for (int i = 1; i <= 64; i++) {
            int bit = ((mask & bitboard) == 0) ? 0 : 1;

            if (i % 8 != 0) {
                System.out.print(bit + "  ");
            } else {
                System.out.println(bit);
            }
            mask = mask >>> 1;
        }
    }


}
