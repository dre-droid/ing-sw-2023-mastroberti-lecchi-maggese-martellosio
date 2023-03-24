package main.java.it.polimi.ingsw.Model;
import java.util.*;
/**
 *  class PersonalGoalCard
 * @author Saverio Maggese
 */

public class PersonalGoalCard{
    /**
     * @param valildTiles è una shelf che contiene unicamente le Tile da matchare
     *
     */
    private Shelf validTiles;


    /**
     * @author Saverio Maggese
     * @param shelf is the player shelf that has to be checked for matches
     * @return an integer corresponding to the points associated with the matces
     */

    public int getPoints(Shelf shelf){
        int counter = o;
        for(int i=0;i<ROWS;i++){
            for(int j=0;j<COLUMNS;j++){
                if(validTiles[i][j].type==shelf[i][j].type{
                    counter++;
                }
            }
        }
        switch(counter){
            case 1: counter==1;
                    return 1;
            case 2: counter==2;
                    return 2;
            case 3: counter==3;
                    return 4;
            case 4: counter==4;
                    return 6;
            case 5: counter==5;
                    return 9;
            case 6: counter==6;
                    return 12;
            default: return 0;


        }



    }

    public Shelf getValidTiles(){
        return this.validTiles;
    }
}