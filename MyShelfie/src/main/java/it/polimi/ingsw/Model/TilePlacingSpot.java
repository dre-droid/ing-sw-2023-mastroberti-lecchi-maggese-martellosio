package main.java.it.polimi.ingsw.Model;

/**
 * @author Francesco Martellosio
 */
public class TilePlacingSpot {
    private Tile tile;
    private boolean available;

    /**
     * Constructor
     * @param tile Tile to be placed in this placing Spot
     * @param available boolean that tells if this TilePlacingSpot is available or not
     */
    public TilePlacingSpot(Tile tile,boolean available){
        this.tile = tile;
        this.available = available;
    }


    /**
     * Sets the availability of the spot
     * @param available boolean value to set the availability of the spot to
     */
    public void setAvailable(boolean available){
        this.available=available;
    }

    /**
     * This method returns wheter this PlacingSpot is available or not
     * @return a boolean, true if the spot is available, false otherwise
     */
    public boolean isAvailable(){
        return available;
    }

    /**
     * This method returns wheter this spot is empty or not
     * @return a boolean, true if the spot is empty, false otherwise
     */
    public boolean isEmpty(){
        return (this.tile ==null);
    }

    /**
     * This method is used to insert a tile in the placingSpot, only if the spot is available
     * @param tileToBePositioned it's the Tile that has to be positioned in this spot
     * @return a boolean that represent the outcome of the operation
     */
    public boolean positionTile(Tile tileToBePositioned){
        if(this.isAvailable()){
            this.tile = tileToBePositioned;
            return true;
        }
        return false;
    }

    /**
     * This method is used to draw the Tile present in the spot, once the Tile is drawn the Spot is emptied
     * @return the tile that was present in this spot, null otherwise
     */
    public Tile drawTileFromSpot() throws InvalidMoveException{
        if(!isAvailable())
            throw new InvalidMoveException("It is not possible to draw from a not available spot!");
        if(!isEmpty()){
            Tile drawnTile = new Tile();
            drawnTile = this.tile;
            this.tile = null;
            return drawnTile;
        }
        else
            return null;
    }

    /**
     * This method is used to Show what tile is in this position
     * @return a copy of the tile in this position or null if the spot is unavailable or empty
     */
    public Tile showTileInThisPosition(){
        if(this.isAvailable() && !this.isEmpty()){
            Tile t = new Tile();
            t = tile;
            return t;
        }
        return null;
    }
}
