package com.baljeet.api;

import com.baljeet.api.Chess.Core.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

@SpringBootTest
public class ChessTest {
    MoveGeneration moveGeneration;
    Board board;
    MersenneTwister mersenneTwister;

    @ParameterizedTest
    @MethodSource("testPositions")
    void testMoveGeneration(String fen, int depth, long expected) {
        new PrecomputedData();
        board = new Board(fen);
        moveGeneration = new MoveGeneration(board);
        long result = numberOfPositionsReached(depth, false);
        assertEquals(expected, result, "Mismatch for FEN: " + fen);
    }
    @ParameterizedTest
    @MethodSource("testPositions")
    void testZobristHash(String fen){
        new PrecomputedData();
        board = new Board(fen);
        moveGeneration = new MoveGeneration(board);
        mersenneTwister = new MersenneTwister(11);
        String fenEnd = null;
        for (int i = 0; i < 30; i++) {
            MoveList moveList = moveGeneration.getAllMoves(false);
            if (moveList.isEmpty()) {
                break;
            }
            long rand = Math.abs((long) mersenneTwister.nextInt());
            int index = (int) rand % moveList.size();
            board.makeMove(moveList.get(index));
            fenEnd = board.toString();
        }
        Board testBoard = new Board(fenEnd);
        System.out.println("Expected board hash: " + testBoard.zobristHash + ". Board hash: " + board.zobristHash);
        assertEquals(testBoard.zobristHash,board.zobristHash, "Mismatch in hashes: Start Fen: " + fen);
    }

    private static Stream<Arguments> testPositions() {
        return Stream.of(
                Arguments.of("rnb1kb2/pp1pqp2/4p1pn/KPp4r/3P3p/4P3/P1P2PPP/RNBQ1BNR w q - 0 12", 5, 21045961L),
                Arguments.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 4, 2103487L),
                Arguments.of("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 4, 3894594L),
                Arguments.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 4, 422333L),
                Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 5, 4865609L),
                Arguments.of("8/2k1p3/3pP3/3P2K1/8/8/8/8 w - - 0 1",6,34834),
                Arguments.of("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1",4,1720476),
                Arguments.of("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",3,89890),
                Arguments.of("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1",5,1004658)
        );
    }
    private static Stream<Arguments> capturesOnlyPositions(){
        return Stream.of(
                Arguments.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 6,2812008),
                Arguments.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 4,757163),
                Arguments.of("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 6, 940350),
                Arguments.of("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",5,2046173)
        );
    }

    public long numberOfPositionsReached(int depth, boolean quiescenceSearch) {
        if (depth == 0) {
            return 1;
        }
        long numberOfPositions = 0;
        MoveList moves = moveGeneration.getAllMoves(quiescenceSearch);
        for (int i = 0;i<moves.size();i++) {
            board.makeMove(moves.get(i));
            numberOfPositions += numberOfPositionsReached(depth - 1, quiescenceSearch);
            board.undoMove(moves.get(i));
        }
        return numberOfPositions;
    }


}
