package com.baljeet.api.Chess.Engine;

import com.baljeet.api.Chess.Core.Board;
import com.baljeet.api.Chess.Core.MoveGeneration;

import java.util.ArrayList;

public class BaljeetEngine implements Engine {
    private final Search search;



    public BaljeetEngine(Board board, ArrayList<Long> gameHistory){
        MoveGeneration moveGeneration = new MoveGeneration(board);
        search = new Search(board, moveGeneration, gameHistory);
    }
    @Override
    public int getBestMove(long time,long increment){
        search.iterativeDeepening(time,increment);
        return search.optimalMove;
    }

    @Override
    public int getEvaluation(long time) {
        return search.iterativeDeepening(time,0L);
    }




}
