package main.java.it.polimi.ingsw.Model;

import java.util.List;

/**
 * @author Diego Lecchi
 * class Shelf represents the shelf of a player as a matrix of Tiles
 */
public class Shelf {
    public final int ROWS = 6;
    public final int COLUMNS = 5;
    private Tile grid[][];

    public Shelf() {
        this.grid = new Tile[ROWS][COLUMNS];
    }

    /**
     * @author Andrea Mastroberti
     * @param t - list of tiles to be inserted
     * @param column - column of the shelf where tiles will be added
     * @throws IndexOutOfBoundsException if the chosen column can't hold all the players tiles
     */
    public void insertTiles(List<Tile> t, int column) throws IndexOutOfBoundsException{
        int size = t.size();
        for (int i = 0; i < ROWS; i++)
            if (grid[i][column] == null){   //first available square
                if ((ROWS - i) < size) throw new IndexOutOfBoundsException("Too many tiles for the selected column!"); //exception if tiles dont fit the column
                else {
                    for (int j = 0; j < size; j++, i++) grid[i][column] = t.remove(size - j - 1);   //add all tiles from the list
                    break;
                }
            }
    }
    public Tile[][] getGrid(){
        return this.grid;
    }

    /**
     * @author Diego Lecchi
     * @return score
     * used to get the score of adjacent tiles of the same type
     */

    public int getAdjScore() {
        // for each element t in the array
        int score=0;

        for (Type t : Type.values()) {
            int adjCounter=0;
            for (int j = 0; j < ROWS; j++) {
                for (int k = 0; k < COLUMNS; k++) {
                    boolean result = false;

                    if(j!=0)    //if not in first row and so on for the rest
                        result = result || checkUp(j, k, t);
                    if(j!=ROWS-1)
                        result = result || checkDown(j, k, t);
                    if(k!=0)
                        result = result || checkLeft(j, k, t);
                    if(k!=COLUMNS-1)
                        result = result || checkRight(j, k, t);
                    if(!result){        //il calcolo va fatto su caselle adiacenti contigue
                        score = score + scoreCalc(adjCounter);
                        adjCounter = 0;
                    }

                }
            }

        }
        return score;
    }

    /**
     * @author Diego Lecchi
     * @param adjCounter -counter of adjacent tiles
     * used to transform the number of adjacent tiles to the respective score for the game
     */
    public int scoreCalc(int adjCounter){

        if(adjCounter<3)
            return 0;

        if(adjCounter==3)
            return 2;

        if(adjCounter==4)
            return 3;

        if(adjCounter==5)
            return 5;

        else
            return 8;

    }

    public boolean checkUp(int j, int k, Type tileType){
        return grid[j-1][k].getType() == tileType;
    }
    public boolean checkDown(int j, int k, Type tileType){
        return grid[j+1][k].getType() == tileType;
    }
    public boolean checkLeft(int j, int k, Type tileType){
        return grid[j][k-1].getType() == tileType;
    }
    public boolean checkRight(int j, int k, Type tileType){
        return grid[j][k+1].getType() == tileType;
    }

    /**
     * @author Diego Lecchi
     * @return true if shelf is full or false otherwise
     */
    public boolean isFull() {
        for (int j = 0; j < ROWS; j++) {
            for (int k = 0; k < COLUMNS; k++) {
                if (grid[j][k] == null)
                    return false;
            }
        }
        return true;
    }
}