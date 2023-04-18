package main.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.Tile;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * class Board represent the board where the tile are positioned
 * @author Francesco martellosio
 */
public class Board {

    private TilePlacingSpot[][] grid;
    private Bag bag;
    private final static int NumOfRows = 9;
    private final static int NumOfColumns = 9;

    public enum Direction {
        UP,
        DOWN,
        RIGHT,
        LEFT
    }

    public Board() {
        bag = new Bag();
        grid = new TilePlacingSpot[NumOfRows][NumOfColumns];
        for (int i = 0; i < NumOfRows; i++) {
            for (int j = 0; j < NumOfColumns; j++) {
                grid[i][j] = new TilePlacingSpot(null, true);
            }
        }
        this.setAlwaysUnavailableSpot();
    }

    /**
     * Constructor of the class, it builds the board setting the available spot based on the number of players
     *
     * @param numOfPlayers number of players playing the game
     */
    public Board(int numOfPlayers) throws InvalidParameterException {
        if (numOfPlayers > 4 || numOfPlayers < 2) {
            throw new InvalidParameterException("There must be 2,3 or 4 players");
        }
        bag = new Bag();
        grid = new TilePlacingSpot[NumOfRows][NumOfColumns];
        for (int i = 0; i < NumOfRows; i++) {
            for (int j = 0; j < NumOfColumns; j++) {
                grid[i][j] = new TilePlacingSpot(null, true);
            }
        }
        this.setAlwaysUnavailableSpot();
        switch (numOfPlayers) {
            case 2:
                this.setUnavailableSpotForTwoPlayersBoard();
                break;
            case 3:
                this.setUnavailableSpotForThreePlayerBoard();
                break;
        }
        this.refill();
    }

    /**
     * This method is used to draw tiles from the board
     *
     * @param x         x coordinate of the starting position on the board [0 ... 8]
     * @param y         y coordinate of the starting position on the board [0 ... 8]
     * @param amount    number of tiles to be drawn
     * @param direction the direction in which to move to draw the tiles
     * @return a list containing the tiles drawn
     * @throws InvalidMoveException      if the selected tiles cannot be drawn according to the game rules
     * @throws InvalidParameterException if the coordinate are wrong
     */
    public List<Tile> drawTiles(int x, int y, int amount, Direction direction) throws InvalidMoveException, InvalidParameterException {
        List<Tile> drawnTiles = new ArrayList<Tile>();
        //checking that the tiles selected don't go out of the matrix or if the selected tiles are not drawable
        int increasingX = x;
        int increasingY = y;
        for (int i = 0; i < amount; i++) {
            if (this.coordinateOutOfBounds(increasingX, increasingY))
                throw new InvalidParameterException("Coordinate out of the matrix");
            if (!this.isThisTileDrawable(increasingX, increasingY))
                throw new InvalidMoveException("Tile in position (" + increasingX + "," + increasingY + ") cannot be drawn");
            switch (direction) {
                case UP:
                    increasingX--;
                    break;
                case DOWN:
                    increasingX++;
                    break;
                case LEFT:
                    increasingY--;
                    break;
                case RIGHT:
                    increasingY++;
                    break;
            }
        }
        //if all the controls are fulfilled we proceed with the extracion of the tile
        for (int i = 0; i < amount; i++) {
            drawnTiles.add(grid[x][y].drawTileFromSpot());
            switch (direction) {
                case UP:
                    x--;
                    break;
                case DOWN:
                    x++;
                    break;
                case LEFT:
                    y--;
                    break;
                case RIGHT:
                    y++;
                    break;
            }

        }
        return drawnTiles;
    }


    /**
     * This private methods checks if the coordinates are valid or out of the bounds of the matrix
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if the coordinates are out of bound, false if they are valid
     * @author Francesco Martellosio
     */
    private boolean coordinateOutOfBounds(int x, int y) {
        if (y < 0 || y > NumOfColumns || x < 0 || x > NumOfRows)
            return true;
        else
            return false;
    }


    /**
     * This method check if a position is empty or alredy occupied by a Tile
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return true if there is not a Tile in the position described by the parameters, false otherwise
     * @author Francesco Martellosio
     */
    public boolean isThisPositionEmpty(int x, int y) {
        return (grid[x][y].isEmpty());
    }

    /**
     * It checks if the Tile can be drawn from the board
     *
     * @param x x coordinate
     * @param y y coordiante
     * @return true if There is at least an empty position in the adiacent cells, false otherwise
     */
    private boolean isThisTileDrawable(int x, int y) {
        if (grid[x][y].isEmpty()) return false;
        for (int i = -1; i <= 1; i += 2) {  //checks tiles to the left, right, up and down
            if (x + i > 9 || y + i > 9 || x + i < 0 || y + i < 0)
                return true;  //if indexes are out of grids bounds, the tile has an empty edge
            else {
                if (grid[x + i][y].isEmpty() || grid[x][y + i].isEmpty()) return true;
            }
        }
        return false;
    }


    /**
     * This method sets to false the flag on the PlacingSpot that are not available in any game mode
     */
    private void setAlwaysUnavailableSpot() {
        this.grid[0][0].setAvailable(false);
        this.grid[0][1].setAvailable(false);
        this.grid[0][2].setAvailable(false);
        this.grid[0][5].setAvailable(false);
        this.grid[0][6].setAvailable(false);
        this.grid[0][7].setAvailable(false);
        this.grid[0][8].setAvailable(false);

        this.grid[1][0].setAvailable(false);
        this.grid[1][1].setAvailable(false);
        this.grid[1][2].setAvailable(false);
        this.grid[1][6].setAvailable(false);
        this.grid[1][7].setAvailable(false);
        this.grid[1][8].setAvailable(false);

        this.grid[2][0].setAvailable(false);
        this.grid[2][1].setAvailable(false);
        this.grid[2][7].setAvailable(false);
        this.grid[2][8].setAvailable(false);

        this.grid[3][0].setAvailable(false);

        this.grid[5][8].setAvailable(false);

        this.grid[6][0].setAvailable(false);
        this.grid[6][1].setAvailable(false);
        this.grid[6][7].setAvailable(false);
        this.grid[6][8].setAvailable(false);

        this.grid[7][0].setAvailable(false);
        this.grid[7][1].setAvailable(false);
        this.grid[7][2].setAvailable(false);
        this.grid[7][6].setAvailable(false);
        this.grid[7][7].setAvailable(false);
        this.grid[7][8].setAvailable(false);

        this.grid[8][0].setAvailable(false);
        this.grid[8][1].setAvailable(false);
        this.grid[8][2].setAvailable(false);
        this.grid[8][3].setAvailable(false);
        this.grid[8][6].setAvailable(false);
        this.grid[8][7].setAvailable(false);
        this.grid[8][8].setAvailable(false);
    }

    /**
     * This method sets to false the flag on the PlacingSpot that are not available in a 2 players game
     */
    private void setUnavailableSpotForTwoPlayersBoard() {
        this.setUnavailableSpotForThreePlayerBoard();
        grid[0][3].setAvailable(false);
        grid[2][6].setAvailable(false);
        grid[3][8].setAvailable(false);
        grid[6][6].setAvailable(false);
        grid[8][5].setAvailable(false);
        grid[6][2].setAvailable(false);
        grid[5][0].setAvailable(false);
        grid[2][2].setAvailable(false);
    }

    /**
     * This method sets to false the flag on the PlacingSpot that are not available in a 3 players game
     */
    private void setUnavailableSpotForThreePlayerBoard() {
        grid[0][4].setAvailable(false);
        grid[1][5].setAvailable(false);
        grid[4][8].setAvailable(false);
        grid[5][7].setAvailable(false);
        grid[8][4].setAvailable(false);
        grid[7][3].setAvailable(false);
        grid[4][0].setAvailable(false);
        grid[3][1].setAvailable(false);
    }

    /**
     * This method clean the board of the remaining tiles, puts them back in the bag and then proceed to refill every available
     * position with new tiles drawn from the bag
     */
    public void refill() {
        if (this.isRefillneeded() == true) {
            //this section clean the board from the remaining Tiles left on the board
            for (int i = 0; i < NumOfRows; i++) {
                for (int j = 0; j < NumOfColumns; j++) {
                    if (grid[i][j].isAvailable() && !grid[i][j].isEmpty()) {
                        Tile toBeShuffledBackInTheBack = new Tile();
                        try {
                            toBeShuffledBackInTheBack = grid[i][j].drawTileFromSpot();
                        } catch (InvalidMoveException e) {
                        }
                        this.bag.reinsertTile(toBeShuffledBackInTheBack);
                    }
                }
            }
            //This section fill all the available spots on the board with new tiles from the bag
            for (int i = 0; i < NumOfRows; i++) {
                for (int j = 0; j < NumOfColumns; j++) {
                    if (grid[i][j].isAvailable()) {
                        grid[i][j].positionTile(bag.pickRandomTile());
                    }
                }
            }
        }
    }

    ;

    /**
     * This method checks if all the Tiles present on the board are isolated prompting the need of a refill of the board
     *
     * @return a boolean value, true if all the Tiles on the board are isolated and false if there is at list one that is not isolated
     */
    private boolean isRefillneeded() {
        boolean anyTileIsNotIsolated = false;
        for (int i = 0; i < NumOfRows; i++) {
            for (int j = 0; j < NumOfColumns; j++) {
                if (grid[i][j].isAvailable() && !grid[i][j].isEmpty()) {
                    //case for the tiles on the first row (the elements above must not be checked because it's out of the matrix
                    if (i == 0) {
                        if (!(grid[i + 1][j].isEmpty() && grid[i][j + 1].isEmpty() && grid[i][j - 1].isEmpty()))
                            anyTileIsNotIsolated = true;
                    }
                    //case for the tiles in the last row (the elements under this line must not be checked because they are out of the grid
                    else if (i == 8) {
                        if (!(grid[i - 1][j].isEmpty() && grid[i][j + 1].isEmpty() && grid[i][j - 1].isEmpty()))
                            anyTileIsNotIsolated = true;
                    }
                    //case for the tiles in the first Column (the element on the left of this line are out of the matrix and therefore must not be checked
                    else if (j == 0) {
                        if (!(grid[i - 1][j].isEmpty() && grid[i + 1][j].isEmpty() && grid[i][j + 1].isEmpty()))
                            anyTileIsNotIsolated = true;
                    }
                    //case for the tiles in the last column (the element on the left of this line are out of the matrix and therefore must not be checked
                    else if (j == 8) {
                        if (!(grid[i - 1][j].isEmpty() && grid[i + 1][j].isEmpty() && grid[i][j - 1].isEmpty()))
                            anyTileIsNotIsolated = true;
                    }
                    //case for all the other tiles
                    else {
                        if (!(grid[i + 1][j].isEmpty() && grid[i - 1][i].isEmpty() && grid[i][j + 1].isEmpty() && grid[i][j - 1].isEmpty()))
                            anyTileIsNotIsolated = true;
                    }
                }

            }
        }
        return (!anyTileIsNotIsolated);
    }

    public void printGridMap() {
        System.out.println("----- Board ------");
        for (int i = 0; i < NumOfRows; i++) {
            for (int j = 0; j < NumOfColumns; j++) {
                if (grid[i][j].isAvailable() == false) System.out.print("X ");
                else {
                    if (grid[i][j].isEmpty()) System.out.print("e ");
                    else {
                        Tile t = grid[i][j].showTileInThisPosition();
                        switch (t.getType()) {
                            case CAT:
                                System.out.print("C ");
                                break;
                            case BOOK:
                                System.out.print("B ");
                                break;
                            case GAME:
                                System.out.print("G ");
                                break;
                            case FRAME:
                                System.out.print("F ");
                                break;
                            case PLANT:
                                System.out.print("P ");
                                break;
                            case TROPHY:
                                System.out.print("T ");
                                break;
                        }
                    }

                }
            }
            System.out.println();
        }
    }

    /*public static void main(String[] args){
        Board b = new Board(4);
        b.printGridMap();
        System.out.println();
        try{
            b.drawTiles(0,3,2,Direction.RIGHT);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        b.printGridMap();
    }*/

    /**
     * this method position a tile in a specific spot (used only for testing)
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void setTile(int x, int y) {
        if (grid[x][y].isAvailable())
            grid[x][y].positionTile(bag.pickRandomTile());
    }

    /**
     * This method returns a copy of the board to display
     *
     * @return a TilePlacingSpot[][] representing the state of the board
     */
    public TilePlacingSpot[][] getBoardForDisplay() {
        TilePlacingSpot[][] boardDisplay = new TilePlacingSpot[NumOfRows][NumOfColumns];
        for (int i = 0; i < NumOfRows; i++)
            for (int j = 0; j < NumOfColumns; j++) {
                boardDisplay[i][j] = new TilePlacingSpot(grid[i][j].showTileInThisPosition(), grid[i][j].isAvailable());
            }
        return boardDisplay;
    }

    /**
     * @return the selected tiles without drawing them
     */
    public List<Tile> getTilesForView(int x, int y, int amount, Direction direction) throws InvalidMoveException {
        List<Tile> drawnTiles = new ArrayList<Tile>();
        //checking that the tiles selected don't go out of the matrix or if the selected tiles are not drawable
        int increasingX = x;
        int increasingY = y;
        for (int i = 0; i < amount; i++) {
            if (this.coordinateOutOfBounds(increasingX, increasingY))
                throw new InvalidParameterException("Coordinate out of the matrix");
            if (!this.isThisTileDrawable(increasingX, increasingY))
                throw new InvalidMoveException("Tile in position (" + increasingX + "," + increasingY + ") cannot be drawn");
            switch (direction) {
                case UP:
                    increasingX--;
                    break;
                case DOWN:
                    increasingX++;
                    break;
                case LEFT:
                    increasingY--;
                    break;
                case RIGHT:
                    increasingY++;
                    break;
            }
        }
        //if all the controls are fulfilled we proceed with adding tile to list
        for (int i = 0; i < amount; i++) {
            drawnTiles.add(grid[x][y].showTileInThisPosition());
            switch (direction) {
                case UP:
                    x--;
                    break;
                case DOWN:
                    x++;
                    break;
                case LEFT:
                    y--;
                    break;
                case RIGHT:
                    y++;
                    break;
            }
        }
        return drawnTiles;

    }
}