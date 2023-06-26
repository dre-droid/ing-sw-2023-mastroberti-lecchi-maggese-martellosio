package main.java.it.polimi.ingsw.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
/**
 *  class PersonalGoalCard is extended by PersonalGoalCardN (N from 1 to 12)
 * @author Saverio Maggese, Diego Lecchi
 */

public class PersonalGoalCard implements Serializable {
    /**
     * @param valildTiles is a shelf that contains only the tiles that the player has to match in order to obtain points
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
        int counter = 0;
        for(int i=0;i<6;i++){
            for(int j=0;j<5;j++){
                if(validTiles.getGrid()[i][j]!=null && shelf.getGrid()[i][j]!= null )
                    if(validTiles.getGrid()[i][j].getType().equals(shelf.getGrid()[i][j].getType())){
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


    @Override
    public String toString() {
        String string = new String();
        Tile[][] s = getValidTiles().getGrid();
        for (Tile[] i : s) {
            string += "       ";
            for (Tile j : i) {
                if (j == null) string += "x ";
                else string += j + " ";
            }
            string += "\n";
        }
        return string;
    }
}