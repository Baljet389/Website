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
        board = new Board(fen, false);
        moveGeneration = new MoveGeneration(board);
        long result = numberOfPositionsReached(depth, false);
        assertEquals(expected, result, "Mismatch for FEN: " + fen);
    }
    @ParameterizedTest
    @MethodSource("chess960Positions")
    void testChess960MoveGeneration(String fen, int depth, long expected) {
        new PrecomputedData();
        board = new Board(fen, true);
        moveGeneration = new MoveGeneration(board);
        long result = numberOfPositionsReached(depth, false);
        assertEquals(expected, result, "Chess 960 Mismatch for FEN: " + fen);
    }
    @ParameterizedTest
    @MethodSource("testPositions")
    void testZobristHash(String fen){
        new PrecomputedData();
        board = new Board(fen, false);
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
        Board testBoard = new Board(fenEnd, false);
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
    private static Stream<Arguments> chess960Positions(){
        return Stream.of(
                Arguments.of("bqnb1rkr/pp3ppp/3ppn2/2p5/5P2/P2P4/NPP1P1PP/BQ1BNRKR w HFhf - 2 9", 5,8146062),
                Arguments.of("2nnrbkr/p1qppppp/8/1ppb4/6PP/3PP3/PPP2P2/BQNNRBKR w HEhe - 1 9", 4,667366),
                Arguments.of("b1q1rrkb/pppppppp/3nn3/8/P7/1PPP4/4PPPP/BQNNRKRB w GE - 1 9", 5, 6417013),
                Arguments.of("qbbnnrkr/2pp2pp/p7/1p2pp2/8/P3PP2/1PPP1KPP/QBBNNR1R w hf - 0 9",5,9183776),
                Arguments.of("1nbbnrkr/p1p1ppp1/3p4/1p3P1p/3Pq2P/8/PPP1P1P1/QNBBNRKR w HFhf - 0 9",4,1171749),
                Arguments.of("qnbnr1kr/ppp1b1pp/4p3/3p1p2/8/2NPP3/PPP1BPPP/QNB1R1KR w HEhe - 1 9", 4, 824055),
                Arguments.of("qbn1brkr/ppp1p1p1/2n4p/3p1p2/P7/6PP/QPPPPP2/1BNNBRKR w HFhf - 0 9",5,13203304),
                Arguments.of("qnnbbrkr/1p2ppp1/2pp3p/p7/1P5P/2NP4/P1P1PPP1/Q1NBBRKR w HFhf - 0 9",5,11110203)

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
