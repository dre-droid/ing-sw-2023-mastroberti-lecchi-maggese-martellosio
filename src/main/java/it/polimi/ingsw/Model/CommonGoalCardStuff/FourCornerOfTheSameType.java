package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.*;

import java.io.Serializable;

public class FourCornerOfTheSameType implements StrategyCommonGoal, Serializable {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        //check that none of the corners are empty
        if (shelfGrid[0][0] != null && shelfGrid[0][4] != null && shelfGrid[5][0] != null && shelfGrid[5][4] != null) {
            //check that all of them have the same type
            if (shelfGrid[0][0].getType() == shelfGrid[0][4].getType() && shelfGrid[5][0].getType() == shelfGrid[5][4].getType() && shelfGrid[0][0].getType() == shelfGrid[5][4].getType())
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "Four tiles of the same type in the four corners of the bookshelf.";
    }

    public int getClassID(){
        return 3;
    }
}
