package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;
import main.java.it.polimi.ingsw.Model.*;

/**
 * @author Saverio Maggese
 * checks if there are two rows containing tiles of all different types
 */
public class TwoLinesOfDifferentTypes implements StrategyCommonGoal{
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        boolean repeated=false;
        int rowcount=0;

        for(int row=0;row<5;row++){
            for(int i=0;i<5;i++){
                for(int j=i+1;j<5;j++){
                    if(shelfGrid[row][i]== null || shelfGrid[row][j]== null)
                        repeated = true;
                    else{
                        if (shelfGrid[row][i] != null && shelfGrid[row][j] != null)
                            if(shelfGrid[row][i].getType() == shelfGrid[row][j].getType()){
                                repeated=true;
                        }
                    }
                }
            }
            if (!repeated)
                rowcount++;
            repeated = false;

        }
        if (rowcount>=2)
            return true ;
        else
            return false;
    }

    @Override
    public String toString(){
        return "Two lines each formed by 5 different types of tiles. One line can show the same or a different combination of the other line.";
    }

    public int getClassID(){
        return 11;
    }
}
