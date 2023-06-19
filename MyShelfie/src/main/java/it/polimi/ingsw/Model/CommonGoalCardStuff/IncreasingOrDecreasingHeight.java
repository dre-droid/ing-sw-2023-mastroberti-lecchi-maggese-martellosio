package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.*;

import java.io.Serializable;

public class IncreasingOrDecreasingHeight implements StrategyCommonGoal, Serializable {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] grid = shelf.getGrid();
        int height = grid.length;
        int width = grid[0].length;

        // Check descending height=6
        for (int col = 0; col < width; col++) {
            boolean columnDesc = true;
            for (int row = 0; row < height - col; row++) {
                if (grid[row][col] == null) {
                    columnDesc = false;
                    break;
                }
            }
            if (columnDesc) {
                return true;
            }
        }

        // Check descending height=5
        for (int col = 0; col < width; col++) {
            boolean columnDesc = true;
            for (int row = 0; row < height - 1 - col; row++) {
                if (grid[row][col] == null) {
                    columnDesc = false;
                    break;
                }
            }
            if (columnDesc) {
                return true;
            }
        }

        // Check ascending height=6
        for (int col = 0; col < width; col++) {
            boolean columnAsc = true;
            for (int row = height - col; row < height; row++) {
                if (grid[row][col] != null) {
                    columnAsc = false;
                    break;
                }
            }
            if (columnAsc) {
                return true;
            }
        }

        // Check ascending height=5
        for (int col = 0; col < width; col++) {
            boolean columnAsc = true;
            for (int row = height - 1 - col; row < height; row++) {
                if (grid[row][col] != null) {
                    columnAsc = false;
                    break;
                }
            }
            if (columnAsc) {
                return true;
            }
        }

        return false;  // None of the conditions satisfied
    }

    @Override
    public String toString(){
        return "Five columns of increasing or decreasing height. Starting from the first column on the left or on the right, each next column must be made of exactly one more tile. Tiles can be of any type.";
    }

    public int getClassID(){
        return 6;
    }

}
