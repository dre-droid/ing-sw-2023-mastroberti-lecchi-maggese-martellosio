package main.java.it.polimi.ingsw.Model;

import java.util.ArrayList;
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
                if ((ROWS - i) < size) throw new IndexOutOfBoundsException("Too many tiles for the selected column!"); //exception if tiles don't fit the column
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
     * @author Francesco Martellosio
     * This method is used to calculate the adjacence points of the shelf
     * @return the points from the current adjacencies
     */
    public int getAdjScore() {
        // for each element t in the array
        int adjCounter=0;
        int scoreCounter=0;
        boolean alreadyCheckedFlag;
        boolean alreadyToBeCheckedFlag;
        //Type maxAdjPtsType;
        class Coordinate{
            public int x,y;
            public Coordinate(int x, int y){
                this.x=x;
                this.y=y;

            }
        }
        List<Coordinate> toBeChecked = new ArrayList<Coordinate>();
        List<Coordinate> alredyChecked = new ArrayList<Coordinate>();

        for(int i=0;i<6;i++){
            for(int j=0;j<5;j++){
                if(grid[i][j]!=null){
                    //System.out.println("NUOVA CELLA ");
                    toBeChecked.add(new Coordinate(i,j));
                    do{
                        Coordinate c = toBeChecked.remove(toBeChecked.size()-1);
                        Coordinate tbc;
                        if(checkUp(c.x,c.y,grid[c.x][c.y].getType())){
                            tbc = new Coordinate(c.x-1,c.y);
                            alreadyCheckedFlag = false;
                            alreadyToBeCheckedFlag = false;
                            for(Coordinate CoordAlredyChecked: alredyChecked){
                                if(CoordAlredyChecked.x ==tbc.x && CoordAlredyChecked.y ==tbc.y)
                                    alreadyCheckedFlag = true;
                            }
                            for(Coordinate CoordAlredyToBeChecked: toBeChecked){
                                if(CoordAlredyToBeChecked.x ==tbc.x && CoordAlredyToBeChecked.y ==tbc.y)
                                    alreadyToBeCheckedFlag = true;
                            }

                            if(!alreadyCheckedFlag && !alreadyToBeCheckedFlag)
                                toBeChecked.add(tbc);
                        }

                        if(checkDown(c.x,c.y,grid[c.x][c.y].getType())){
                            tbc = new Coordinate(c.x+1,c.y);
                            alreadyCheckedFlag=false;
                            alreadyToBeCheckedFlag = false;
                            for(Coordinate CoordAlredyChecked: alredyChecked){
                                if(CoordAlredyChecked.x ==tbc.x && CoordAlredyChecked.y ==tbc.y)
                                    alreadyCheckedFlag = true;
                            }
                            for(Coordinate CoordAlredyToBeChecked: toBeChecked){
                                if(CoordAlredyToBeChecked.x ==tbc.x && CoordAlredyToBeChecked.y ==tbc.y)
                                    alreadyToBeCheckedFlag = true;
                            }
                            if(!alreadyCheckedFlag && !alreadyToBeCheckedFlag)
                                toBeChecked.add(tbc);
                        }

                        if(checkLeft(c.x,c.y,grid[c.x][c.y].getType())){
                            tbc = new Coordinate(c.x,c.y-1);
                            alreadyCheckedFlag = false;
                            alreadyToBeCheckedFlag = false;
                            for(Coordinate CoordAlredyChecked: alredyChecked){
                                if(CoordAlredyChecked.x ==tbc.x && CoordAlredyChecked.y ==tbc.y)
                                    alreadyCheckedFlag = true;
                            }
                            for(Coordinate CoordAlredyToBeChecked: toBeChecked){
                                if(CoordAlredyToBeChecked.x ==tbc.x && CoordAlredyToBeChecked.y ==tbc.y)
                                    alreadyToBeCheckedFlag = true;
                            }
                            if(!alreadyCheckedFlag && !alreadyToBeCheckedFlag)
                                toBeChecked.add(tbc);
                        }

                        if(checkRight(c.x,c.y,grid[c.x][c.y].getType())){
                            tbc = new Coordinate(c.x,c.y+1);
                            alreadyCheckedFlag = false;
                            alreadyToBeCheckedFlag = false;
                            for(Coordinate CoordAlredyChecked: alredyChecked){
                                if(CoordAlredyChecked.x ==tbc.x && CoordAlredyChecked.y ==tbc.y)
                                    alreadyCheckedFlag = true;
                            }
                            for(Coordinate CoordAlredyToBeChecked: toBeChecked){
                                if(CoordAlredyToBeChecked.x ==tbc.x && CoordAlredyToBeChecked.y ==tbc.y)
                                    alreadyToBeCheckedFlag = true;
                            }
                            if(!alreadyCheckedFlag && !alreadyToBeCheckedFlag)
                                toBeChecked.add(tbc);
                        }
                        adjCounter+=1;
                        alredyChecked.add(c);
                        /*System.out.println("Tile controllata: ("+c.x+","+c.y+")");
                        System.out.println("Tile già controllate: ");
                        for(Coordinate stampa : alredyChecked){System.out.println("("+stampa.x+","+stampa.y+")");}
                        System.out.println("Tile da controllare: ");
                        for(Coordinate stampa : toBeChecked){System.out.println("("+stampa.x+","+stampa.y+")");}
                        System.out.println();*/
                    }while(toBeChecked.size()!=0);
                    //System.out.println("Tessere adiacenti: "+ adjCounter);
                    scoreCounter+=scoreCalc(adjCounter);
                    toBeChecked.clear();
                    adjCounter=0;
                }


            }
        }
        return scoreCounter;
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

    /**
     *These checkX functions check if the tileType of the Tile that we are looking at is equals to the one at its left/right
     * or the one above/below
     * @param j line
     * @param k column
     * @param tileType type of the current Tile
     * @return true if the tileType of the current tile is equal to the checked one
     */
    public boolean checkUp(int j, int k, Type tileType){
        if(j-1<0) return false;
        if(grid[j-1][k] != null)
            return grid[j-1][k].getType() == tileType;
        else
            return false;
    }
    public boolean checkDown(int j, int k, Type tileType) {
        if(j+1>5) return false;
        if(grid[j+1][k] != null)
            return grid[j+1][k].getType() == tileType;
        else
            return false;
    }
    public boolean checkLeft(int j, int k, Type tileType) {
        if(k-1<0) return false;
        if(grid[j][k-1] != null)
            return grid[j][k-1].getType() == tileType;
        else
            return false;
    }
    public boolean checkRight(int j, int k, Type tileType){
        if(k+1>4) return false;
        if(grid[j][k+1] != null)
            return grid[j][k+1].getType() == tileType;
        else
            return false;
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

    /**
     * This method returns a copy of the grid
     * @return a Tile matrix that is the copy of the grid of the shelf
     */
    public Tile[][] getGridForDisplay(){
        Tile[][] displayGrid = new Tile[ROWS][COLUMNS];
        for(int i=0;i<ROWS;i++)
            for(int j=0;j<COLUMNS;j++){
                if(grid[i][j]!=null){
                    System.out.print(grid[i][j]+ " ");
                    displayGrid[i][j] = new Tile(grid[i][j].getType());
                }
                else{
                    System.out.print("x ");
                }
                System.out.println();
            }
        return displayGrid;
    }
}