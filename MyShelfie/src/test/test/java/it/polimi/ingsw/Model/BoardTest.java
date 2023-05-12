package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;
import main.java.it.polimi.ingsw.Model.Tile;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    @Test
    public void Constructor_2PlayersMode_29TileSpotFull(){
        Board b = new Board(2);
        int count = 0;
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
                if(!b.isThisPositionEmpty(i, j))count+=1;
        assertEquals(29,count);
    }

    @Test
    public void Constructor_3PlayersMode_37TileSpotFull(){
        Board b = new Board(3);
        int count = 0;
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
                if(!b.isThisPositionEmpty(i, j))count+=1;
        assertEquals(37,count);
    }

    @Test
    public void Constructor_4PlayersMode_45TileSpotFull(){
        Board b = new Board(4);
        int count = 0;
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
                if(!b.isThisPositionEmpty(i, j))count+=1;
        assertEquals(45,count);
    }

    @Test
    public void Constructor_WrongNumberOfPlayers_InvalidParameterExceptionIsThrown(){
        assertThrows(InvalidParameterException.class,()->{Board b = new Board(7);});
    }

    @Test
    public void drawTiles_ValidPositionAndAmount_ReturnedListIsNotNull(){
        Board b = new Board(2);
        List<Tile> drawnTiles = new ArrayList<Tile>();
        try{
            drawnTiles = b.drawTiles(1,3,2, Board.Direction.RIGHT);
        }catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(drawnTiles);
    }

    @Test
    public void drawTiles_ValidPositionAndAmount_CorrectNumberOfTilesReturned(){
        Board b = new Board(2);
        List<Tile> drawnTiles = new ArrayList<Tile>();
        try{
            drawnTiles = b.drawTiles(1,3,2, Board.Direction.RIGHT);
        }catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        assertEquals(2,drawnTiles.size());
    }

    @Test
    public void drawTiles_ValidPositionAndAmount_TheSpotsAreEmptiedAfterTheDraw(){
        Board b = new Board(2);
        List<Tile> drawnTiles = new ArrayList<Tile>();
        try{
            drawnTiles = b.drawTiles(1,3,2, Board.Direction.RIGHT);
        }catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        int numOfEmptySpots=0;
        if(b.isThisPositionEmpty(1,3))numOfEmptySpots++;
        if(b.isThisPositionEmpty(1,4))numOfEmptySpots++;
        assertEquals(2,numOfEmptySpots);

    }
    @Test
    public void drawTiles_ValidPositionAndAmount_TheSpotsAreEmptiedAfterTheDraw_LEFT(){
        Board b = new Board(2);
        List<Tile> drawnTiles = new ArrayList<Tile>();
        try{
            drawnTiles = b.drawTiles(1,4,2, Board.Direction.LEFT);
        }catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        int numOfEmptySpots=0;
        if(b.isThisPositionEmpty(1,3))numOfEmptySpots++;
        if(b.isThisPositionEmpty(1,4))numOfEmptySpots++;
        assertEquals(2,numOfEmptySpots);

    }
    @Test
    public void drawTiles_ValidPositionAndAmount_TheSpotsAreEmptiedAfterTheDraw_DOWN(){
        Board b = new Board(2);
        List<Tile> drawnTiles = new ArrayList<Tile>();
        try{
            drawnTiles = b.drawTiles(4,1,2, Board.Direction.DOWN);
        }catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        int numOfEmptySpots=0;
        if(b.isThisPositionEmpty(4,1))numOfEmptySpots++;
        if(b.isThisPositionEmpty(5,1))numOfEmptySpots++;
        assertEquals(2,numOfEmptySpots);

    }
    @Test
    public void drawTiles_ValidPositionAndAmount_TheSpotsAreEmptiedAfterTheDraw_UP(){
        Board b = new Board(2);
        List<Tile> drawnTiles = new ArrayList<Tile>();
        try{
            drawnTiles = b.drawTiles(5,1,2, Board.Direction.UP);
        }catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        int numOfEmptySpots=0;
        if(b.isThisPositionEmpty(4,1))numOfEmptySpots++;
        if(b.isThisPositionEmpty(5,1))numOfEmptySpots++;
        assertEquals(2,numOfEmptySpots);

    }

    @Test
    public void drawTiles_CoordinateOutOfBound_InvalidParameterExceptionIsThrown() {
        Board b = new Board(3);
        assertThrows(InvalidParameterException.class,()->{b.drawTiles(3,42,2, Board.Direction.RIGHT);});
    }

    @Test
    public void drawTiles_NotAvailablePlacingSpotIsSelected_InvalidMoveExceptionIsThrown(){
        Board b = new Board(2);
        assertThrows(InvalidMoveException.class,()->{b.drawTiles(0,0,1, Board.Direction.RIGHT);});
    }

    @Test
    public void drawTiles_NotAvailablePlacingSpotInTheTilesSelected_InvalidMoveExceptionIsThrown(){
        Board b = new Board(2);
        assertThrows(InvalidMoveException.class,()->{b.drawTiles(1,3,3, Board.Direction.RIGHT);});
    }

    @Test
    public void drawTiles_SomeOfTheSelectedTilesAreNotDrawable_InvalidMoveExceptionIsThrown(){
        Board b = new Board(2);
        assertThrows(InvalidMoveException.class,()->{b.drawTiles(1,4,2, Board.Direction.DOWN);});
    }


    @Test
    public void drawTiles_TryingToDrawFromAnEmptySpot_InvalidMoveExceptionIsThrown(){
        Board b = new Board(2);
        try{
            b.drawTiles(1,3,2, Board.Direction.RIGHT);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        assertThrows(InvalidMoveException.class,()->{b.drawTiles(1,3,2, Board.Direction.RIGHT);});
    }

    @Test
    public void refill_OnlyIsolatedCardsRemain_BoardGetActuallyRefilled(){
        Board b = new Board();
        b.setTile(5,3);
        b.setTile(2,3);
        b.refill();
        int count = 0;
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
                if(!b.isThisPositionEmpty(i, j))count+=1;
        assertEquals(45,count );
    }
    @Test
    public void getTilesForView_RIGHT_Test(){
        Board b = new Board(2);
        List<Tile> drawnTiles;
        List<Tile> viewTiles;
        try{
            viewTiles = b.getTilesForView(1, 3, 2, Board.Direction.RIGHT);
            drawnTiles = b.drawTiles(1,3,2, Board.Direction.RIGHT);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        assertEquals(drawnTiles, viewTiles);
    }
    @Test
    public void getTilesForView_throws_InvalidParameterException_Test(){
        Board b = new Board(2);
        assertThrows(InvalidParameterException.class, ()->{b.getTilesForView(20, 20, 2, Board.Direction.RIGHT);} );
    }
    @Test
    public void getTilesForView_throws_InvalidMoveException_Test(){
        Board b = new Board(2);
        assertThrows(InvalidMoveException.class, ()->{b.getTilesForView(1, 2, 2, Board.Direction.RIGHT);} );
    }
    @Test
    public void getTilesForView_LEFT_Test(){
        Board b = new Board(2);
        List<Tile> drawnTiles;
        List<Tile> viewTiles;
        try{
            viewTiles = b.getTilesForView(1, 4, 2, Board.Direction.LEFT);
            drawnTiles = b.drawTiles(1,4,2, Board.Direction.LEFT);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        assertEquals(drawnTiles, viewTiles);
    }
    @Test
    public void getTilesForView_DOWN_Test(){
        Board b = new Board(2);
        List<Tile> drawnTiles;
        List<Tile> viewTiles;
        try{
            viewTiles = b.getTilesForView(4, 1, 2, Board.Direction.DOWN);
            drawnTiles = b.drawTiles(4,1,2, Board.Direction.DOWN);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        assertEquals(drawnTiles, viewTiles);
    }
    @Test
    public void getTilesForView_UP_Test(){
        Board b = new Board(2);
        List<Tile> drawnTiles;
        List<Tile> viewTiles;
        try{
            viewTiles = b.getTilesForView(5, 1, 2, Board.Direction.UP);
            drawnTiles = b.drawTiles(5,1,2, Board.Direction.UP);
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
        assertEquals(drawnTiles, viewTiles);
    }

}