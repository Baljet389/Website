package com.baljeet.api.Maze;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
public class Maze {
    int height;
    int width;
    int passageLength = 1;
    int nextCell;
    Cell[] cell;
    int[] nextcell = new int[4];
    Random rand = new Random();
    Stack<Integer> stack = new Stack<>();
    ArrayList<Integer> aktCells = new ArrayList<>();

    Maze(int height, int width) {
        int aktCell = rand.nextInt( height * width - 1);
        this.height = height;
        this.width = width;
        cell = new Cell[height * width];
        for (int i = 0; i < (height * width); i++) {
            cell[i] = new Cell(i);
        }

        while (passageLength < height * width) {
                stack.push(aktCell);
                aktCell = chooseNeighbour(aktCell);
                passageLength++;
                if(aktCell ==-1){
                   aktCell= backtrack();
                }
            }
        config();
    }
    int chooseNeighbour(int aktCell) {

        nextcell[0] = aktCell + 1;
        nextcell[1] = aktCell - 1;
        nextcell[2] = aktCell + width;
        nextcell[3] = aktCell - width;

        for (int i = 0; i < nextcell.length; i++) {
            int randomIndexToSwap = rand.nextInt(nextcell.length);
            int temp = nextcell[randomIndexToSwap];
            nextcell[randomIndexToSwap] = nextcell[i];
            nextcell[i] = temp;
        }
        for (int i = 0; i < 4; i++) {
            nextCell = nextcell[i];
            if (aktCell + 1 == nextCell && (cell[aktCell].position() + 1) % width != 0 && !cell[nextCell].visitedGenerate) {
                cell[aktCell].carvePassage(3);
                cell[nextCell].carvePassage(1);
                return nextCell;
            }


            if (aktCell - 1 == nextCell && cell[aktCell].position() % width != 0 && !cell[nextCell].visitedGenerate) {
                cell[aktCell].carvePassage(1);
                cell[nextCell].carvePassage(3);
                return nextCell;
            }
            if (aktCell + width == nextCell && cell[aktCell].position() < ((width * height) - width) && !cell[nextCell].visitedGenerate) {
                cell[aktCell].carvePassage(2);
                cell[nextCell].carvePassage(0);
                return nextCell;
            }
            if (aktCell - width == nextCell && cell[aktCell].position() > width && !cell[nextCell].visitedGenerate) {
                cell[aktCell].carvePassage(0);
                cell[nextCell].carvePassage(2);
                return nextCell;
            }

        }

        return -1;


    }
    void config(){
            cell[0].setCost(Manhattan(0, height * width - 1));
            cell[0].setWayLength(0);
            aktCells.add(0);
            cell[0].visitedSolve = true;
    }
    int backtrack() {
        int next = -1;
        while(!stack.isEmpty()) {
            next = chooseNeighbour(stack.pop());
            if(next != -1){
                return next;
            }
        }
        return next;

    }
    void solveMaze() {
        int aktCellIndex = 0;
        while(!cell[height * width - 1].visitedSolve) {
            int maxCost = Integer.MAX_VALUE;
            int s = aktCells.get(aktCellIndex);
            nextcell[0] = s - width;
            nextcell[1] = s - 1;
            nextcell[2] = s + width;
            nextcell[3] = s + 1;
            for (int i = 0; i < 4; i++) {
                if(cell[s].activeWalls(i) && !cell[nextcell[i]].visitedSolve) {
                    cell[nextcell[i]].setCost(Manhattan(nextcell[i], height * width - 1) + cell[s].getWayLength() + 1);
                    aktCells.add(nextcell[i]);
                    cell[nextcell[i]].setReferenceCell(s);
                }
            }
            aktCells.remove(aktCellIndex);


            for (int i = 0; i < aktCells.size(); i++) {
                if(cell[aktCells.get(i)].getCost() < maxCost) {
                    maxCost = cell[aktCells.get(i)].getCost();
                    aktCellIndex = i;
                }
            }
            cell[aktCells.get(aktCellIndex)].visitedSolve = true;

        }
            int lastCell = aktCells.get(aktCellIndex);
            while (!cell[0].fastestPath) {
                cell[lastCell].fastestPath = true;
                lastCell = cell[lastCell].getReferenceCell();
            }

    }
    int Manhattan(int cell1, int cell2) {
        return (cell2 % width - cell1 % width + cell2 / width - cell1 / width);

    }


}
