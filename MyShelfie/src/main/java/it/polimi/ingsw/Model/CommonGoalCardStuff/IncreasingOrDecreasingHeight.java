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
        boolean columnDesc6 = true;
        boolean columnDesc5 = true;
        boolean columnAsc6 = true;
        boolean columnAsc5 = true;
        // Check descending height=6
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height - col; row++) {
                if (grid[row][col] == null) {
                    columnDesc6 = false;
                    break;
                }
            }
            if (columnDesc6) {
                return true;
            }
        }

        // Check descending height=5
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height - 1 - col; row++) {
                if (grid[row][col] == null) {
                    columnDesc5 = false;
                    break;
                }
            }
            if (columnDesc5) {
                return true;
            }
        }

        // Check ascending height=6
        for (int col = 0; col < width; col++) {
            for (int row = 0; row <= col; row++) {
                if (grid[row][col] == null) {
                    columnAsc6 = false;
                    break;
                }
            }
            if (columnAsc6) {
                return true;
            }
        }

        // Check ascending height=5
        for (int col = 0; col < width; col++) {
            for (int row = 0; row <= col + 1; row++) {
                if (grid[row][col] == null) {
                    columnAsc5 = false;
                    break;
                }
            }
            if (columnAsc5) {
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
