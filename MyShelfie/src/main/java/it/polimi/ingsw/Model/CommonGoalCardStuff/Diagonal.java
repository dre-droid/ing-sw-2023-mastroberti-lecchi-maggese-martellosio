package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;
import main.java.it.polimi.ingsw.Model.*;

/**
 * @author Saverio Maggese
 * checks if the diagonal common goal is metched
 */

public class Diagonal implements StrategyCommonGoal {
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        Type type00, type10,type50, type40;
        int counter = 0;
        if(shelfGrid[0][0]!=null){
            type00 = shelfGrid[0][0].getType();

            for (int i = 0; i < 5; i++) {
                if(shelfGrid[i][i]!=null)
                    if (type00 == shelfGrid[i][i].getType())
                        counter++;
            }
            if (counter == 5)
                return true;
        }
        if(shelfGrid[1][0]!=null){
            type10 = shelfGrid[1][0].getType();
            counter = 0;
            for (int i = 1; i <= 5; i++) {
                if(shelfGrid[i][i-1]!=null)
                    if (type10 == shelfGrid[i][i-1].getType()) {
                        counter++;
                    }
            }
            if (counter == 5)
                return true;
        }
        if(shelfGrid[5][0]!=null){
            type50 = shelfGrid[5][0].getType();
            counter = 0;
            for (int i = 5; i > 0; i--) {
                if(shelfGrid[i][5-i]!=null)
                    if (type50 == shelfGrid[i][5 - i].getType()) {
                        counter++;
                    }
            }
            if (counter == 5)
                return true;
        }
        if(shelfGrid[4][0]!=null){
            type40 = shelfGrid[4][0].getType();
            counter = 0;
            for (int i = 4; i >= 0; i--) {
                if(shelfGrid[i][4-i]!=null)
                    if (type40 == shelfGrid[i][4 - i].getType()) {
                        counter++;
                    }
            }
            if (counter == 5)
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "Five tiles of the same type forming a diagonal";
    }

    public int getClassID(){
        return 1;
    }
}
