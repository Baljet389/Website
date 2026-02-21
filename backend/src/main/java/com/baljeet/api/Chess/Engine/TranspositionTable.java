package com.baljeet.api.Chess.Engine;

public class TranspositionTable {

    private final TTEntry[] tt;
    private final int tableSize;

    public record TTEntry(
            long zobristKey,
            int bestMove,
            int depth,
            int score,
            byte boundType,
            int age
    ) {}

    TranspositionTable(int tableSize) {
        this.tableSize = tableSize;
        tt = new TTEntry[tableSize];
    }

    public void store(long zobristKey, int move, int score, int depth, byte bound, int age) {
        int index = (int) (zobristKey & (tableSize - 1));
        TTEntry entry = tt[index];

        // deeper searches and old ones are overwritten
        if (entry == null || entry.depth() < depth || Math.abs(entry.age() - age) > 5) {
            tt[index] = new TTEntry(zobristKey, move, depth, score, bound, age);
        }
    }

    public TTEntry lookup(long zobristKey) {
        TTEntry entry = tt[(int) (zobristKey & (tableSize - 1))];
        if (entry == null) return null;
        if (entry.zobristKey() == zobristKey) return entry;
        return null;
    }
}