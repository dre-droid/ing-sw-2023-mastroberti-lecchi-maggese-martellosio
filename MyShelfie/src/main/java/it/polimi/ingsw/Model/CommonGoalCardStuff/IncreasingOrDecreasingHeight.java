package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.*;

public class IncreasingOrDecreasingHeight implements StrategyCommonGoal{
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        boolean desc=true;
        boolean cresc=true;
        for(int col=0;col<5;col++){
            if(shelfGrid[5-col][col] != null){
                desc=false;

            }
        }
        if(desc){
            for(int col=0;col<5;col++){
                for(int raw=0;raw<5-col;raw++){
                    if(shelfGrid[raw][col] == null){
                        desc=false;
                    }
                }
            }
        }
        for(int col=0;col<5;col++){
            if(shelfGrid[col+1][col] != null){
                cresc=false;

            }
        }
        if(cresc){
            for(int col=0;col<5;col++){
                for(int raw=0;raw<=col;raw++){
                    if(shelfGrid[raw][col] == null){
                        cresc=false;
                    }
                }
            }
        }
        return(cresc || desc);



    }
}
