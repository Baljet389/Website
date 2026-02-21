package com.baljeet.api.Chess.Engine;

import com.baljeet.api.Chess.Core.Board;
import com.baljeet.api.Chess.Core.MoveGeneration;
import com.baljeet.api.Chess.Core.MoveList;
import com.baljeet.api.Chess.Core.Piece;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Search {

    private final MoveGeneration moveGeneration;
    private final Board board;
    private final Evaluation evaluation;

    private int searchDepth;
    private long start;
    private long timeForMove;
    private long nodesSearched;
    private long quiescenceNodes;
    private boolean searchCancelled;
    private int optimalMoveIteration;
    public int optimalMove;
    public int eval;
    private TranspositionTable.TTEntry entry;
    private final ArrayList<Long> gameHistory;
    private static final Logger logger = LoggerFactory.getLogger(Search.class);

    private int[][] killerMoves;

    TranspositionTable tt;
    // Must use power of two
    private final int tableSize = 1 << 19;
    private static final byte EXACT = 0;
    private static final byte LOWER_BOUND = 1;
    private static final byte UPPER_BOUND = 2;
    private static final int maxSearchExtensions = 2;

    public Search(Board board, MoveGeneration moveGeneration, ArrayList<Long> gameHistory) {
        this.board = board;
        this.moveGeneration = moveGeneration;
        this.gameHistory = gameHistory;
        evaluation = new Evaluation(board);
        tt = new TranspositionTable(tableSize);
    }

    public int iterativeDeepening(long time, long increment) {
        if (board.fullMoveNumber < 10) {
            int move = OpeningDatabase.lookupPosition(moveGeneration.getAllMoves(false) , board.toString());
            if (move != 0) {
                optimalMove = move;
                return 0;
            }
        }

        logger.debug("******* SEARCH STARTED ********");
        logger.debug("FEN: {}", board);
        boolean mateFound = false;
        searchCancelled = false;
        nodesSearched = 0;
        quiescenceNodes = 0;

        timeForMove = chooseTimeForMove(time, increment);
        start = System.currentTimeMillis();
        logger.debug("Allocated time: {}", timeForMove);

        for (int i = 1; i < 20; i++) {
            searchDepth = i;
            killerMoves = new int[i + 1 + maxSearchExtensions][2];
            int evalIteration = negaMax(Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2, searchDepth, 0, 0);
            if (searchCancelled) break;
            else {
                optimalMove = optimalMoveIteration;
                eval = evalIteration;
            }
            logger.debug("Depth: {} Eval: {} Best Move: {}", i, eval, MoveList.moveToString(optimalMoveIteration));

            if (eval > 90000) {
                mateFound = true;
                break;
            }


        }
        logger.debug("Mate Found: {}", mateFound);
        logger.debug("Time used [ms]: {}", (System.currentTimeMillis() - start));
        logger.debug("Time remaining [ms]: {}", (timeForMove - System.currentTimeMillis() + start));
        logger.debug("Nodes searched: {}", nodesSearched);
        logger.debug("Quiescence Nodes searched: {}", quiescenceNodes);
        logger.debug("******* SEARCH ENDED ********");
        return eval;

    }

    private int negaMax(int alpha, int beta, int depth, int searchExtensions, int plyFromRoot) {
        if (searchCancelled) return 0;
        nodesSearched++;
        // Check every 1024 nodes if time is up
        if ((nodesSearched & 0x3FF) == 0 && System.currentTimeMillis() - start > timeForMove) {
            searchCancelled = true;
            return 0;
        }
        // Check if evaluated position is already in TT
        entry = tt.lookup(board.zobristHash);
        if (entry != null && entry.depth() >= searchDepth - plyFromRoot && plyFromRoot != 0) {
            if (entry.boundType() == EXACT) return entry.score();
            // New lower bound (beta)
            else if (entry.boundType() == LOWER_BOUND && entry.score() >= beta) return entry.score();
            // New upper bound (alpha)
            else if (entry.boundType() == UPPER_BOUND && entry.score() <= alpha) return entry.score();
        }

        MoveList moveList = moveGeneration.getAllMoves(false);
        if (moveList.isEmpty()) {
            if (moveGeneration.check) {
                return -100000 + 100 * (searchDepth - depth); // checkmate
            } else {
                return 0; // stalemate
            }
        }

        // Static evaluation
        if (depth == 0) return quiescenceSearch(alpha, beta);
        // if (depth == 0) return evaluation.evaluate();
        int max = Integer.MIN_VALUE / 2;
        sortMoves(moveList, depth, plyFromRoot);

        int initialAlpha = alpha;
        int bestMove = 0;
        for (int i = 0; i < moveList.size(); i++) {
            int move = moveList.get(i);
            board.makeMove(move);
            int score;
            if (board.halfMoveClock >= 100 || (board.halfMoveClock >= 4 && checkForRepetition())) {
                score = 0;
            }
            else {
                int extensions = 0;
                if(searchExtensions < maxSearchExtensions){
                    if (board.isInCheck()) extensions = 1;
                }
                score = -negaMax(-beta, -alpha, depth - 1 + extensions, searchExtensions + extensions, plyFromRoot +1);
            }
            board.undoMove(move);
            if (score > max) {
                max = score;
                bestMove = move;
                if (plyFromRoot == 0) {
                    optimalMoveIteration = move;
                }
                alpha = Math.max(max, alpha);
            }
            if (score >= beta) {
                if(MoveList.getFlag(move) != Piece.CAPTURE && killerMoves[plyFromRoot][0] != move){
                        killerMoves[plyFromRoot][1] = killerMoves[plyFromRoot][0];
                        killerMoves[plyFromRoot][0] = move;
                }
                return score;
            }

        }
        byte bound;
        if (max <= initialAlpha) {
            bound = UPPER_BOUND;
        } else if (max >= beta) {
            bound = LOWER_BOUND;
        } else {
            bound = EXACT;
        }
        if (!searchCancelled) tt.store(board.zobristHash, bestMove, max, searchDepth - plyFromRoot, bound, board.fullMoveNumber);
        return max;
    }

    private long chooseTimeForMove(long timeLeft, long increment) {

        long buffer = 100;
        long base = (timeLeft - buffer) / 30;

        long timeForMove = base + increment / 2;

        //Never use more than 10 seconds
        return Math.min(timeForMove, 10000);
    }

    // Move ordering
    private void sortMoves(MoveList moveList, int depth, int plyFromRoot) {
        // First: PV-Move from prev iteration
        // Second: Move from Transposition Table
        // Third: Captures
        // Fourth: Normal Moves
        int[] scores = new int[moveList.size()];
        entry = tt.lookup(board.zobristHash);
        
        for (int i = 0; i < moveList.size(); i++) {
            int move = moveList.get(i);
            int score = getScore(depth, move, plyFromRoot);
            scores[i] = score;

        }
        for (int i = 0; i < moveList.size() - 1; i++) {
            int maxIndex = i;

            for (int j = i + 1; j < moveList.size(); j++) {
                if (scores[j] > scores[maxIndex]) {
                    maxIndex = j;
                }
            }
            int tempScore = scores[i];
            scores[i] = scores[maxIndex];
            scores[maxIndex] = tempScore;

            int tempMove = moveList.get(i);
            moveList.set(i, moveList.get(maxIndex));
            moveList.set(maxIndex, tempMove);
        }

    }

    private int getScore(int depth, int move, int plyFromRoot) {
        int to = MoveList.getTo(move);
        int from = MoveList.getFrom(move);
        int score = 0;
        int[] PST;
        int phase = evaluation.getPhase();

        switch(board.currentPosition[from]){
            case Piece.PAWN -> {
                 PST = board.whiteToMove ? EvaluationData.W_PAWN_PST:EvaluationData.B_PAWN_PST;
                 score += PST[to]- PST[from];
            }
            case Piece.KNIGHT -> score+= EvaluationData.KNIGHT_PST[to] - EvaluationData.KNIGHT_PST[from];
            case Piece.BISHOP -> {
                PST = board.whiteToMove ? EvaluationData.W_BISHOP : EvaluationData.B_BISHOP;
                score += PST[to] - PST[from];
            }
            case Piece.KING ->
                score += board.whiteToMove ?
                          evaluation.getBlendedPSTScore(1L << to, phase, EvaluationData.W_KING_PST_MIDDLE, EvaluationData.B_KING_PST_END)
                        - evaluation.getBlendedPSTScore(1L << from, phase, EvaluationData.W_KING_PST_MIDDLE, EvaluationData.W_KING_PST_END)
                        : evaluation.getBlendedPSTScore(1L << to, phase, EvaluationData.B_KING_PST_MIDDLE, EvaluationData.B_KING_PST_END)
                        - evaluation.getBlendedPSTScore(1L << from, phase, EvaluationData.B_KING_PST_MIDDLE, EvaluationData.B_KING_PST_END);
        }
        if (depth == searchDepth && move == optimalMove) {
            score += 10000;
        } else if (entry != null && entry.bestMove() == move) {
            score += 9000;
        } else if (MoveList.getFlag(move) == Piece.CAPTURE) {

            score += EvaluationData.MVVLVA[board.currentPosition[to]][board.currentPosition[from]];
        }
        if(move == killerMoves[plyFromRoot][0] || move == killerMoves[plyFromRoot][1]){
            score += 8000;
        }
        return score;
    }

    private boolean checkForRepetition() {
        long currentHash = board.zobristHash;
        int count = 0;

        // Only look back as far as the half-move clock allows
        int limit = Math.min(gameHistory.size(), board.halfMoveClock + 1);

        for (int i = gameHistory.size() - limit; i < gameHistory.size(); i++) {
            if (gameHistory.get(i) == currentHash) {
                count++;
                if (count >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private int quiescenceSearch(int alpha, int beta){
        if (searchCancelled) return 0;
        quiescenceNodes++;
        // Check every 1024 nodes if time is up
        if ((quiescenceNodes & 0x3FF) == 0 && System.currentTimeMillis() - start > timeForMove) {
            searchCancelled = true;
            return 0;
        }
        int e = evaluation.evaluate();
        if (e >= beta) return beta;
        if (e > alpha) alpha = e;

        int max = Integer.MIN_VALUE / 2;

        MoveList moves = moveGeneration.getAllMoves(true);
        if(moves.isEmpty()) return evaluation.evaluate();
        sortCapturesOnlyMoves(moves);

        for (int i = 0; i < moves.size(); i++){
            int move = moves.get(i);
            board.makeMove(move);
            int score = -quiescenceSearch(-beta, -alpha);
            board.undoMove(move);
            if(score > max){
                max = score;
                alpha = Math.max(score,alpha);
            }
            if (score >= beta){
                return score;
            }
        }
        return max;
    }
    private void sortCapturesOnlyMoves(MoveList moveList){
        int[] scores = new int[moveList.size()];

        for (int i = 0; i < moveList.size(); i++) {
            int move = moveList.get(i);
            int attacker = board.currentPosition[MoveList.getFrom(move)];
            int victim =  board.currentPosition[MoveList.getTo(move)];

            int score = EvaluationData.MVVLVA[victim][attacker];
            scores[i] = score;

        }
        for (int i = 0; i < moveList.size() - 1; i++) {
            int maxIndex = i;

            for (int j = i + 1; j < moveList.size(); j++) {
                if (scores[j] > scores[maxIndex]) {
                    maxIndex = j;
                }
            }
            int tempScore = scores[i];
            scores[i] = scores[maxIndex];
            scores[maxIndex] = tempScore;

            int tempMove = moveList.get(i);
            moveList.set(i, moveList.get(maxIndex));
            moveList.set(maxIndex, tempMove);
        }
    }
}
