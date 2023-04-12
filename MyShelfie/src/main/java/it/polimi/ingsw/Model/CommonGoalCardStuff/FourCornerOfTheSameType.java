package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.*;

public class FourCornerOfTheSameType implements StrategyCommonGoal{
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        if(shelfGrid[0][0].getType()!= null && shelfGrid[0][4].getType()!=null && shelfGrid[5][0].getType()!=null && shelfGrid[5][4].getType()!= null){
            if(shelfGrid[0][0].getType()==shelfGrid[0][4].getType())
                if(shelfGrid[0][4].getType()==shelfGrid[5][0].getType())
                    if(shelfGrid[5][0].getType()==shelfGrid[5][4].getType())
                        return true;}
        return false;
    }

    @Override
    public String toString(){
        return "Four tiles of the same type in the four corners of the bookshelf.";
    }
}
