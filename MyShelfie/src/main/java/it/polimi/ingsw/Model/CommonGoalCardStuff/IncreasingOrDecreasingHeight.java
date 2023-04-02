package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.*;

public class IncreasingOrDecreasingHeight implements StrategyCommonGoal{
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] grid = shelf.getGrid();
        boolean asc1, desc1,asc2, desc2;
        int tileInPreviousColumn=0;
        int tileInThisColumn =0;
        desc1 = true;
        asc1 = true;
        asc2=true;
        desc2=true;
        //desc heigth==6
        for(int col =0;col<5;col++){
            for(int row=0;row<6-col;row++){
                if(grid[row][col]==null){
                    desc1 = false;
                }
            }
            for(int row = 6-col;row<6;row++){
                if(grid[row][col]!=null){
                    desc1 = false;
                }
            }
        }
        //desc heigth=5
        for(int col =0;col<5;col++){
            for(int row=0;row<5-col;row++){
                if(grid[row][col]==null){
                    desc2 = false;
                }
            }
            for(int row = 5-col;row<6;row++){
                if(grid[row][col]!=null){
                    desc2 = false;
                }
            }
        }

        //asc heigth=6
        for(int col =0;col<5;col++){
            for(int row=0;row<6-4+col;row++){
                if(grid[row][col]==null){
                    asc1 = false;
                }
            }
            for(int row = 6-4+col;row<6;row++){
                if(grid[row][col]!=null){
                    asc1 = false;
                }
            }
        }
        //asc heigth=5
        for(int col =0;col<5;col++){
            for(int row=0;row<5-4+col;row++){
                if(grid[row][col]==null){
                    asc2 = false;
                }
            }
            for(int row = 5-4+col;row<6;row++){
                if(grid[row][col]!=null){
                    asc2 = false;
                }
            }
        }

        return (desc1 || desc2 || asc1 || asc2);


    }

}
