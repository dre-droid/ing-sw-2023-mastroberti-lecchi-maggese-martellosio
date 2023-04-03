package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;

public class ThreeColumnsOfMaxThreeDifferentTypes implements StrategyCommonGoal  {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        int colcount=0;
        int flagCat=0;
        int flagBook=0;
        int flagGame=0;
        int flagFrame=0;
        int flagTrophy=0;
        int flagPlant=0;
        int sum=0;
        int tilesInThisColumn=0;

        for(int col=0;col<5;col++){
            for(int raw=0;raw<6;raw++){
                if(shelfGrid[raw][col]!= null){
                    switch (shelfGrid[raw][col].getType()) {
                        case CAT:flagCat=1;break;
                        case BOOK:flagBook=1;break;
                        case GAME:flagGame=1;break;
                        case FRAME:flagFrame=1;break;
                        case PLANT:flagPlant=1;break;
                        case TROPHY:flagTrophy=1;break;

                    }
                    tilesInThisColumn++;
                }


            }
            sum = flagCat + flagBook + flagFrame + flagPlant + flagTrophy + flagGame;
            if(sum <= 3 && tilesInThisColumn==6){
                colcount++;
            }
            System.out.println("Different tiles in this column "+sum);
            System.out.println("Colcount: "+colcount);
            flagCat = 0;
            flagBook = 0;
            flagGame = 0;
            flagFrame = 0;
            flagPlant = 0;
            flagTrophy =0;
            sum=0;
            tilesInThisColumn=0;
        }
        return colcount>=3 ;







    }
}
