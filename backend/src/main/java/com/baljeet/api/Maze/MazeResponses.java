package com.baljeet.api.Maze;


import java.util.ArrayList;
import java.util.List;

public class MazeResponses {
   public static class GenerateResponse {

        public int w;

        GenerateResponse(int w) {
            this.w = w;
        }

        public static List<GenerateResponse> respond(Cell[] cells, int width) {

            List<GenerateResponse> response = new ArrayList<>();
            for (Cell i : cells) {

                if ((i.position % width + i.position / width) % 2 == 0) {
                    int result = 0;
                    for (boolean bit : i.walls) {
                        result <<= 1;            // Shift result left by 1 bit
                        if (bit) result |= 1;        // Set the last bit if current boolean is true
                    }
                    response.add(new GenerateResponse(result));
                }
            }
            return response;
        }
    }
    public static class SolveResponse{
         public int p;

         public SolveResponse(int p) {
             this.p = p;
         }

         public static SolveResponse[] respond(Cell[] cells, int height, int width) {

             SolveResponse[] response = new SolveResponse[height * width];
             for (Cell i : cells) {
                 response[i.position] = new SolveResponse(i.fastestPath ? 1 : 0);
             }
             return response;
         }
     }

}
