package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;


import main.java.it.polimi.ingsw.Model.*;

import java.io.Serializable;

/**
 * @author Saverio Maggese
 * checks if there are at least 8 tiles of the same type
 */

public class EightofSameType implements StrategyCommonGoal, Serializable {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        int countCat=0;
        int countBook=0;
        int countGame=0;
        int countFrame=0;
        int countTrophy=0;
        int countPlant=0;
        for(int i=0;i<6;i++){
            for(int j=0;j<5;j++){
                if(shelfGrid[i][j]!= null) {
                    switch (shelfGrid[i][j].getType()) {
                        case CAT:
                            countCat++;
                            break;
                        case BOOK:
                            countBook++;
                            break;
                        case GAME:
                            countGame++;
                            break;
                        case FRAME:
                            countFrame++;
                            break;
                        case PLANT:
                            countPlant++;
                            break;
                        case TROPHY:
                            countTrophy++;
                            break;

                    }


                }

            }
        }
        if(countCat >=8 || countBook>=8 || countFrame>=8 || countTrophy>=8 || countPlant>=8 || countGame>=8){
            return true;
        }else{
        return false;}

    }

    @Override
    public String toString(){
        return "Eight tiles of the same type. There’s no restriction about the position of these tiles.";
    }

    public int getClassID(){
        return 2;
    }
}
