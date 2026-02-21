package com.baljeet.api.Chess.Core;

public class MoveList {

    //1-6:from; 7-12:to; 13-16:flags

    private static final int MAX_MOVES = 256;
    private final int[] moves = new int[MAX_MOVES];
    private int size = 0;

    public void add(int move) {
        moves[size++] = move;
    }

    public int get(int index) {
        return moves[index];
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    public int[] raw() {
        return moves;
    }

    //i must be < moves.size()
    public void set(int i,int move){
        moves[i] = move;
    }

    public boolean isEmpty(){
        return size == 0;
    }

    public static int packMove(int from, int to, int flag) {
        return  (from | (to<<6) | (flag << 12));
    }

    public static int getFrom(int move) {
        return move & 0x3f;
    }
    public static int getTo(int move) {
        return (move & 0xfc0) >>> 6;
    }

    public static int getFlag(int move) {
        return (move & 0xf000) >>>12;
    }


    public static String moveToString(int move) {
        return "From: " + Board.indexToSquare(getFrom(move)) + " To: " + Board.indexToSquare(getTo(move)) + " Flags: " + getFlag(move);
    }
}
