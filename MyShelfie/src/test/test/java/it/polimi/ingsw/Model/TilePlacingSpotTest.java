package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.InvalidMoveException;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.TilePlacingSpot;
import main.java.it.polimi.ingsw.Model.Type;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TilePlacingSpotTest {
    @Test
    public void positionTile_AvailableSpot_OutcomeOfTheOperationTrue(){
        Tile tile = new Tile(Type.CAT);
        TilePlacingSpot spot = new TilePlacingSpot(null,true);
        assertTrue(spot.positionTile(tile));
    }

    @Test
    public void positionTile_NotAvailableSpot_OutcomeOfTheOperationFalse(){
        Tile tile = new Tile(Type.CAT);
        TilePlacingSpot spot = new TilePlacingSpot(null,false);
        assertFalse(spot.positionTile(tile));
    }

    @Test
    public void positionTile_AvailableSpot_TileActuallyPositioned() throws InvalidMoveException{
        Tile tile = new Tile(Type.CAT);
        TilePlacingSpot spot = new TilePlacingSpot(null,true);
        spot.positionTile(tile);
        assertEquals(tile, spot.drawTileFromSpot());
    }

    @Test
    public void drawTileFromSpot_EmptyAvailableSpot_DrawnTileIsNull() throws InvalidMoveException {
        TilePlacingSpot spot = new TilePlacingSpot(null,true);
        assertEquals(null, spot.drawTileFromSpot());
    }

    @Test
    public void drawTileFromSpot_NotEmptyAvailableSpot_TileIsCorrectlyDrawn() throws InvalidMoveException{
        Tile tile = new Tile(Type.CAT);
        TilePlacingSpot spot = new TilePlacingSpot(tile,true);
        assertEquals(tile, spot.drawTileFromSpot());
    }

    @Test
    public void drawTileFromSpot_NotEmptyAvailableSpot_SpotIsEmptyAfterWards() throws InvalidMoveException{
        Tile tile = new Tile(Type.CAT);
        TilePlacingSpot spot = new TilePlacingSpot(tile,true);
        spot.drawTileFromSpot();
        assertTrue(spot.isEmpty());
    }

    @Test
    public void drawTileFromSpot_NotAvailableSpot_InvalidMoveExceptionIsThrown(){
        Tile tile = new Tile(Type.CAT);
        TilePlacingSpot spot = new TilePlacingSpot(tile,false);
        assertThrows(InvalidMoveException.class, ()->{spot.drawTileFromSpot();});
    }
}