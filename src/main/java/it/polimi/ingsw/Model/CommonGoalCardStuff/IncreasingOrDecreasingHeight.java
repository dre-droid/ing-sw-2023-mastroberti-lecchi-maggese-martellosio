package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.*;

import java.io.Serializable;

public class IncreasingOrDecreasingHeight implements StrategyCommonGoal, Serializable {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] grid = shelf.getGrid();
        int height = 6;
        int width = 5;
        boolean columnDesc6 = true;
        boolean nullaboveDesc6 = false;
        boolean columnDesc5 = true;
        boolean nullaboveDesc5 = false;
        boolean columnAsc6 = true;
        boolean nullaboveAsc6 = false;
        boolean columnAsc5 = true;
        boolean nullaboveAsc5 = false;
        // Check descending height=6
        for (int col = 0; col < width; col++) {
            for (int row = 5; row >= col; row--) {
                if (grid[row][col] == null) {
                    columnDesc6 = false;
                    break;
                }
            }

        }
        if(grid[0][1] == null && grid[1][2] == null && grid[2][3] == null && grid[3][4] == null){
            nullaboveDesc6 = true;

        }
        if (columnDesc6 && nullaboveDesc6) {
            return true;
        }

        // Check descending height=5
        for (int col = 0; col < width; col++) {
            for (int row = 5; row > col; row--) {
                if (grid[row][col] == null) {
                    columnDesc5 = false;
                    break;
                }

            }

        }
        if(grid[0][0] == null && grid[1][1] == null && grid[2][2] == null && grid[3][3] == null && grid[4][4] == null){
            nullaboveDesc5 = true;

        }
        if (columnDesc5 && nullaboveDesc5) {
            return true;
        }

        // Check ascending height=6
        for (int col = 0; col < width; col++) {
            for (int row = 5; row >= 4 - col; row--) {
                if (grid[row][col] == null) {
                    columnAsc6 = false;
                    break;
                }
            }

        }
        if(grid[3][0] == null && grid[2][1] == null && grid[1][2] == null && grid[0][3] == null){
            nullaboveAsc6 = true;

        }
        if (columnAsc6 && nullaboveAsc6) {
            return true;
        }

        // Check ascending height=5
        for (int col = 0; col < width; col++) {
            for (int row = 5; row > 4 - col; row--) {
                if (grid[row][col] == null) {
                    columnAsc5 = false;
                    break;
                }
            }

        }
        if(grid[4][0] == null && grid[3][1] == null && grid[2][2] == null && grid[1][3] == null && grid[0][4]==null){
            nullaboveAsc5 = true;

        }
        if (columnAsc5 && nullaboveAsc5) {
            return true;
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
