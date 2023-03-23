package main.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * abstract class Board
 * @author Francesco martellosio
 */
abstract class Board{
    private Tile[][] grid;
    //private Bag bag;

    public enum Direction{
        UP,
        DOWN,
        RIGHT,
        LEFT
    }


    public Board(){

    }

    /**
     *
     * @param start starting point from where to start drawing tiles from the board
     * @param amount amount of tiles drawn
     * @param direction the direction in which the tiles are drawn (up, down, righ, left)
     * @return a list containing the drawn tiles
     * @throws IndexOutOfBoundsException if the start coordinates are out of the matrix bounds or if the final coordinates
     * are out of the matrix bounds
     *
     */
    public List<Tile> drawTiles(int[] start, int amount,Direction direction) throws IndexOutOfBoundsException, InvalidMoveException{

        int x,y;
        x = start[0];
        y = start[1];
        List<Tile> drawnTiles = new ArrayList<Tile>();

        if(coordinateOutOfBounds(x,y)){
            throw new IndexOutOfBoundsException();
        }

        for(int i=0; i<amount; i++){
            if(isThisPositionEmpty(x,y)){
                throw new InvalidMoveException("No Tile in this position");
            }
            drawnTiles.add(grid[x][y]);
            grid[x][y]=null;


            switch(direction){
                case UP: y--; break;
                case RIGHT: x++; break;
                case DOWN: y++; break;
                case LEFT: x--; break;
            }
            if(coordinateOutOfBounds(x,y)){
                throw new IndexOutOfBoundsException();
            }
        }
        return drawnTiles;
    }

    /**
     * This private methods checks if the coordinates are valid or out of the bounds of the matrix
     * @author Francesco Martellosio
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the coordinates are out of bound, false if they are valid
     */
    protected boolean coordinateOutOfBounds(int x, int y){
        int nrows, ncolumn;
        nrows= grid.length;
        ncolumn = grid[0].length;

        if(y<0 || y>nrows || x<0 || x>ncolumn)
            return true;
        else
            return false;
    }


    /**
     * This protected method check if a position is empty or alredy occupied by a Tile
     * @author Francesco Martellosio
     * @param x x coordinate
     * @param y y coordinate
     * @return true if there is not a Tile in the position described by the parameters, false otherwise
     */
    protected boolean isThisPositionEmpty(int x, int y){
        return (grid[x][y]==null);
    }

    /**
     * It checks if the Tile can be drawn from the board
     * @param x x coordinate
     * @param y y coordiante
     * @return true if There is at least an empty position in the adiacent cells, false otherwise
     */
    protected boolean isThisTileDrawable(int x, int y){
        return (grid[x-1][y]==null || grid[x+1][y]==null || grid[x][y-1]==null || grid[x][y+1]==null);
    }

    abstract public void Refill();
}