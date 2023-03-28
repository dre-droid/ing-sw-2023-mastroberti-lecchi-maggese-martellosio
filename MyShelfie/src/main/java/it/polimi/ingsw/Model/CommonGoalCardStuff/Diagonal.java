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
        Type type00 = shelfGrid[0][0].getType();
        Type type10 = shelfGrid[1][0].getType();
        Type type50 = shelfGrid[5][0].getType();
        Type type40 = shelfGrid[4][0].getType();


        int counter = 0;

        for (int i = 0; i < 5; i++) {
            if (type00 == shelfGrid[i][i].getType())
                counter++;
        }
        if (counter == 5)
            return true;
        counter = 0;
        for (int i = 1; i <= 5; i++) {
            if (type10 == shelfGrid[i][i-1].getType()) {
                counter++;
            }
        }
        if (counter == 5)
            return true;
        counter = 0;

        for (int i = 5; i > 0; i--) {
            if (type50 == shelfGrid[i][5 - i].getType()) {
                counter++;
            }
        }
        if (counter == 5)
            return true;
        counter = 0;
        for (int i = 4; i >= 0; i--) {
            if (type50 == shelfGrid[i][4 - i].getType()) {
                counter++;
            }
        }
        if (counter == 5)
            return true;

        return false;




    }
}
