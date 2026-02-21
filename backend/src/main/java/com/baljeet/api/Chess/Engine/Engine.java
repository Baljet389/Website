package com.baljeet.api.Chess.Engine;

public interface Engine {
    int getBestMove(long time,long increment );
    int getEvaluation(long time);
}
