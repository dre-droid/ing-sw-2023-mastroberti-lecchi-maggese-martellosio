package main.java.it.polimi.ingsw.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
/**
 *  class PersonalGoalCard
 * @author Saverio Maggese
 */

public abstract class PersonalGoalCard{
    /**
     * @param valildTiles è una shelf che contiene unicamente le Tile da matchare
     *
     */
    protected Shelf validTiles;

    public PersonalGoalCard() {this.validTiles = new Shelf();}

    /**
     * @author Saverio Maggese
     * @param shelf is the player shelf that has to be checked for matches
     * @return an integer corresponding to the points associated with the matches
     */
    public int getPoints(Shelf shelf){
        initializeValidTiles();
        int counter = 0;
        for(int i=0;i<6;i++){
            for(int j=0;j<5;j++){
                if(validTiles.getGrid()[i][j]!=null && shelf.getGrid()[i][j]!= null )
                    if(validTiles.getGrid()[i][j].getType()==shelf.getGrid()[i][j].getType()){
                        counter++;
                }
            }
        }
        switch(counter){
            case 1: return 1;
            case 2: return 2;
            case 3: return 4;
            case 4: return 6;
            case 5: return 9;
            case 6: return 12;
            default: return 0;


        }



    }

    public Shelf getValidTiles(){
        return this.validTiles;
    }

    public abstract void initializeValidTiles();
}