package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;


import main.java.it.polimi.ingsw.Model.*;
/**
 * @author Saverio Maggese
 * checks if there are at least tiles of the same type
 */

public class EightofSameType implements StrategyCommonGoal{
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        int countCat=0;
        int countBook=0;
        int countGame=0;
        int countFrame=0;
        int countTrophy=0;
        int countPlant=0;
        for(int i=0;i<5;i++){
            for(int j=0;j<6;j++){
                switch (shelfGrid[i][j].getType()) {
                    case CAT:countCat++;
                    case BOOK:countBook++;
                    case GAME:countGame++;
                    case FRAME:countFrame++;
                    case PLANT:countPlant++;
                    case TROPHY:countTrophy++;

                }
                if(countCat >=8 || countBook>=8 || countFrame>=8 || countTrophy>=8 || countPlant>=8 || countGame>=8){
                    return true;
                }



            }
        }
        return false;

    }
}
