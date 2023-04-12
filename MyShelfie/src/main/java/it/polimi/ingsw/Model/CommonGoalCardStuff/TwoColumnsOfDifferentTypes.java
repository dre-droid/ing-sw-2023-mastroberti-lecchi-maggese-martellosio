package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;
import main.java.it.polimi.ingsw.Model.*;

/**
 * @author Saverio Maggese
 * checks if there are two columns containing tiles of all different types
 */
public class TwoColumnsOfDifferentTypes implements StrategyCommonGoal{
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        boolean repeated=false;
        int colcount=0;

        for(int col=0;col<5;col++){
            for(int i=0;i<6;i++){
                for(int j=i+1;j<6;j++){
                    if(shelfGrid[i][col]== null || shelfGrid[j][col]== null)
                        repeated = true;
                    else{
                        if(shelfGrid[i][col].getType() == shelfGrid[j][col].getType()){
                            repeated=true;
                        }
                    }


                    }
                }
            if (!repeated)
                colcount++;
            repeated = false;

        }
        if (colcount>=2)
            return true ;
        else
            return false;
    }

    @Override
    public String toString(){
        return "Two columns each formed by 6 different types of tiles.";
    }


}
