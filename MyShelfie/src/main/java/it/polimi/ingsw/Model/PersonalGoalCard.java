package main.java.it.polimi.ingsw.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.*;
/**
 *  class PersonalGoalCard is extended by PersonalGoalCardN (N from 1 to 12)
 * @author Saverio Maggese, Diego Lecchi
 */

public class PersonalGoalCard{
    /**
     * @param valildTiles is a shelf that contains only the tiles that the player has to match in order to obtain points
     *
     */
    protected Shelf validTiles;

    public PersonalGoalCard() {this.validTiles = new Shelf();}
/*
    public void initializeValidTiles(int type){
        switch(type){
            case 1:{
                this.validTiles.getGrid()[0][0] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[0][2] = new Tile(Type.FRAME);
                this.validTiles.getGrid()[1][4] = new Tile(Type.CAT);
                this.validTiles.getGrid()[2][3] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[3][1] = new Tile(Type.GAME);
                this.validTiles.getGrid()[5][2] = new Tile(Type.TROPHY);
            }break;
            case 2:{
                this.validTiles.getGrid()[1][1] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[2][0] = new Tile(Type.CAT);
                this.validTiles.getGrid()[2][2] = new Tile(Type.GAME);
                this.validTiles.getGrid()[3][4] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[4][3] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[5][4] = new Tile(Type.FRAME);
            }break;
            case 3:{
                this.validTiles.getGrid()[2][2] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[3][1] = new Tile(Type.CAT);
                this.validTiles.getGrid()[1][3] = new Tile(Type.GAME);
                this.validTiles.getGrid()[5][0] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[3][4] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[1][0] = new Tile(Type.FRAME);
            }break;
            case 4:{
                this.validTiles.getGrid()[3][3] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[4][2] = new Tile(Type.CAT);
                this.validTiles.getGrid()[0][4] = new Tile(Type.GAME);
                this.validTiles.getGrid()[4][1] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[2][0] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[2][2] = new Tile(Type.FRAME);
            }break;
            case 5:{
                this.validTiles.getGrid()[4][4] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[5][3] = new Tile(Type.CAT);
                this.validTiles.getGrid()[5][0] = new Tile(Type.GAME);
                this.validTiles.getGrid()[3][2] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[1][1] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[3][1] = new Tile(Type.FRAME);
            }break;
            case 6:{
                this.validTiles.getGrid()[5][0] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[0][4] = new Tile(Type.CAT);
                this.validTiles.getGrid()[4][1] = new Tile(Type.GAME);
                this.validTiles.getGrid()[2][3] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[0][2] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[4][3] = new Tile(Type.FRAME);
            }break;
            case 7:{
                this.validTiles.getGrid()[2][1] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[0][0] = new Tile(Type.CAT);
                this.validTiles.getGrid()[4][4] = new Tile(Type.GAME);
                this.validTiles.getGrid()[5][2] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[3][0] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[1][3] = new Tile(Type.FRAME);
            }break;
            case 8:{
                this.validTiles.getGrid()[3][0] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[1][1] = new Tile(Type.CAT);
                this.validTiles.getGrid()[5][3] = new Tile(Type.GAME);
                this.validTiles.getGrid()[4][3] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[2][2] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[0][4] = new Tile(Type.FRAME);
            }break;
            case 9:{
                this.validTiles.getGrid()[4][4] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[2][2] = new Tile(Type.CAT);
                this.validTiles.getGrid()[0][2] = new Tile(Type.GAME);
                this.validTiles.getGrid()[3][4] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[4][1] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[5][0] = new Tile(Type.FRAME);
            }break;
            case 10:{
                this.validTiles.getGrid()[5][3] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[3][3] = new Tile(Type.CAT);
                this.validTiles.getGrid()[1][1] = new Tile(Type.GAME);
                this.validTiles.getGrid()[2][0] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[0][4] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[4][1] = new Tile(Type.FRAME);
            }break;
            case 11:{
                this.validTiles.getGrid()[0][2] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[4][4] = new Tile(Type.CAT);
                this.validTiles.getGrid()[2][0] = new Tile(Type.GAME);
                this.validTiles.getGrid()[1][1] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[5][3] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[3][2] = new Tile(Type.FRAME);
            }break;
            case 12:{
                this.validTiles.getGrid()[1][1] = new Tile(Type.PLANT);
                this.validTiles.getGrid()[5][0] = new Tile(Type.CAT);
                this.validTiles.getGrid()[4][4] = new Tile(Type.GAME);
                this.validTiles.getGrid()[0][2] = new Tile(Type.BOOK);
                this.validTiles.getGrid()[3][3] = new Tile(Type.TROPHY);
                this.validTiles.getGrid()[2][2] = new Tile(Type.FRAME);
            }break;
        }
    }

 */

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