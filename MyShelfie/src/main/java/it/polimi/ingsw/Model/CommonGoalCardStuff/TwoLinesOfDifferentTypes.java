package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;
import main.java.it.polimi.ingsw.Model.*;

/**
 * @author Saverio Maggese
 * checks if there are two raws containing tiles of all different types
 */
public class TwoLinesOfDifferentTypes implements StrategyCommonGoal{
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        boolean repeated=false;
        int rawcount=0;

        for(int raw=0;raw<5;raw++){
            for(int i=0;i<5;i++){
                for(int j=i+1;j<5;j++){
                    if(shelfGrid[raw][i].getType() != null && shelfGrid[raw][j].getType()!= null)
                        if(shelfGrid[raw][i].getType() == shelfGrid[raw][j].getType()){
                            repeated=true;



                        }

                }
            }
            if (!repeated)
                rawcount++;
            repeated = false;

        }
        if (rawcount>=2)
            return true ;
        else
            return false;

    }
}
