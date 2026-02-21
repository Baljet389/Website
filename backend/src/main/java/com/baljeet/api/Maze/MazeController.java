package com.baljeet.api.Maze;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;


@RestController
@RequestMapping("/api/projects/maze")
public class MazeController {
    private Maze currentMaze = null;
    int width;
    int height;


    @PostMapping("/generate")
    public ResponseEntity<List<MazeResponses.GenerateResponse>> postMaze(@RequestBody MazeRequests.GenerateMazeRequest request) {
        if (request.width <= 0 || request.height <= 0) {
            return ResponseEntity.badRequest().build();
        }
        this.width = request.width;
        this.height = request.height;
        currentMaze = new Maze(height,width);
        List<MazeResponses.GenerateResponse> response= MazeResponses.GenerateResponse.respond(currentMaze.cell, width);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/solve")
    public ResponseEntity<MazeResponses.SolveResponse[]> getMaze() {
        if (currentMaze == null) {
            return ResponseEntity.badRequest().build();
        }
        currentMaze.solveMaze();
        MazeResponses.SolveResponse[] response = MazeResponses.SolveResponse.respond(currentMaze.cell,height, width);
        return ResponseEntity.ok(response);
    }

}
