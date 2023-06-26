package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;

import java.io.Serializable;

public class FourRowsOfMaxThreeDifferentTypes implements StrategyCommonGoal , Serializable {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        int rowcount=0;
        int flagCat=0;
        int flagBook=0;
        int flagGame=0;
        int flagFrame=0;
        int flagTrophy=0;
        int flagPlant=0;
        int sum=0;
        int tilesInRow=0;

        for(int row=0;row<6;row++){
            for(int col=0;col<5;col++){
                if(shelfGrid[row][col]!= null){
                    switch (shelfGrid[row][col].getType()) {
                        case CAT:flagCat=1;break;
                        case BOOK:flagBook=1;break;
                        case GAME:flagGame=1;break;
                        case FRAME:flagFrame=1;break;
                        case PLANT:flagPlant=1;break;
                        case TROPHY:flagTrophy=1;break;

                    }
                    tilesInRow++;
                }
            }
            sum = flagCat + flagBook + flagFrame + flagPlant + flagTrophy + flagGame;
            //System.out.println("tipi diversi in questa linea: "+sum);
            if(sum <= 3 && tilesInRow==5){
                rowcount++;
            }
            //System.out.println("rowcount: "+ rowcount);
            flagCat = 0;
            flagBook = 0;
            flagGame = 0;
            flagFrame = 0;
            flagPlant = 0;
            flagTrophy =0;
            sum=0;
            tilesInRow=0;

        }
        return rowcount>=4;
    }

    @Override
    public String toString(){
        return "Four lines each formed by 5 tiles of maximum three different types. One line can show the same or a different combination of another line.";
    }

    public int getClassID(){
        return 5;
    }
}
