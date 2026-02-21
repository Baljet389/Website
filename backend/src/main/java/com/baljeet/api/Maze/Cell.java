package com.baljeet.api.Maze;

public class Cell {
    public int position;//0- width*height-1
    public boolean[] walls = new boolean[4];//false = wall; true = no wall
    public boolean fastestPath = false;
    boolean visitedGenerate = false;
    boolean visitedSolve = false;
    private int referenced;
    private int cost;
    private int wayLength;

    Cell(int position) {
        this.position = position;

    }

    boolean activeWalls(int i) {
        return walls[i];
    }

    int position() {
        return position;
    }

    void carvePassage(int x) {
        visitedGenerate = true;
        switch (x) {
            case (0):
                walls[0] = true;
                return;
            case (1):
                walls[1] = true;
                return;
            case (2):
                walls[2] = true;
                return;
            case (3):
                walls[3] = true;
        }


    }

    public void setCost(int cost) {
        cost = this.cost;
    }

    public int getCost() {
        return cost;
    }

    public int getReferenceCell() {
        return referenced;
    }

    public void setReferenceCell(int referenced) {
        this.referenced = referenced;
    }

    public int getWayLength() {
        return wayLength;
    }

    public void setWayLength(int wayLength) {
        this.wayLength = wayLength;
    }
}
