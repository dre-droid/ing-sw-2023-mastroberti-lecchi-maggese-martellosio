package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.*;

public class FourRawsOfMaxThreeDifferentTypes implements StrategyCommonGoal {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        int rawcount=0;
        int flagCat=0;
        int flagBook=0;
        int flagGame=0;
        int flagFrame=0;
        int flagTrophy=0;
        int flagPlant=0;
        int sum=0;

        for(int raw=0;raw<6;raw++){
            for(int col=0;col<5;col++){
                if(shelfGrid[raw][col]!= null)
                    switch (shelfGrid[raw][col].getType()) {
                        case CAT:flagCat=1;
                        case BOOK:flagBook=1;
                        case GAME:flagGame=1;
                        case FRAME:flagFrame=1;
                        case PLANT:flagPlant=1;
                        case TROPHY:flagTrophy=1;

                    }

            }
            sum = flagCat + flagBook + flagFrame + flagPlant + flagTrophy + flagGame;
            if(sum <= 3){
                rawcount++;
            }
            flagCat = 0;
            flagBook = 0;
            flagGame = 0;
            flagFrame = 0;
            flagPlant = 0;
            flagTrophy =0;
            sum=0;

        }
        return rawcount>=4;









    }
}
