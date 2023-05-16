package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.*;

import java.io.Serializable;

public class XShapedTiles implements StrategyCommonGoal , Serializable {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        for(int i = 1;i < 5;i++){
            for(int j = 1;j < 4;j++){
                if( shelfGrid[i][j]!= null && shelfGrid[i+1][j+1]!= null && shelfGrid[i-1][j-1]!= null && shelfGrid[i+1][j-1]!= null && shelfGrid[i-1][j+1]!= null){
                    if(shelfGrid[i][j].getType()==shelfGrid[i+1][j+1].getType()){
                        if(shelfGrid[i][j].getType()==shelfGrid[i-1][j-1].getType()){
                            if(shelfGrid[i][j].getType()==shelfGrid[i+1][j-1].getType()){
                                if(shelfGrid[i][j].getType()==shelfGrid[i-1][j+1].getType()){
                                    return true;
                                }
                            }
                        }
                    }

                }

            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Five tiles of the same type forming an X.";
    }

    public int getClassID(){
        return 12;
    }
}
