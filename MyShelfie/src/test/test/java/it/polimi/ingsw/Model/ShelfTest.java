package test.java.it.polimi.ingsw.Model;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.Tile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShelfTest {

    @Test
    @DisplayName("Test for fun")
    public void firstTest(){
        assert 1 + 1 == 2;
    }

    @Test
    public void isFull_emptyShelf_returnFalse() {
         Shelf shelf = new Shelf();
         assertFalse(shelf.isFull());
    }

    @Test
    public void isFull_fullShelf_returnTrue() {
        Shelf shelf = new Shelf();
        Tile[][] grid = shelf.getGrid();
        for (int j = 0; j < shelf.ROWS; j++) {
            for (int k = 0; k < shelf.COLUMNS; k++) {
                grid[j][k] = new Tile(Type.CAT);
            }
        }
        assertTrue(shelf.isFull());
    }

    @Test
    public void getAdjScore_lessThan3AdjTiles_return0(){

        Shelf shelf = new Shelf();
        Tile[][] grid = shelf.getGrid();
        grid[0][0] = new Tile(Type.CAT);
        grid[0][1] = new Tile(Type.CAT);

        assertEquals(0, shelf.getAdjScore());
    }

    @Test
    public void getAdjScore_equal4AdjTiles_return3(){

        Shelf shelf = new Shelf();
        Tile[][] grid = shelf.getGrid();
        grid[0][0] = new Tile(Type.CAT);
        grid[0][1] = new Tile(Type.CAT);
        grid[1][1] = new Tile(Type.CAT);
        grid[2][1] = new Tile(Type.CAT);

        assertEquals(3, shelf.getAdjScore());
    }

    @Test
    public void getAdjScore_6AdjacentCatAnd4AdjacentGames_11PointsReturned(){
        Shelf shelf = new Shelf();
        Tile[][] grid = shelf.getGrid();

        grid[0][0] = new Tile(Type.CAT);
        grid[0][1] = new Tile(Type.CAT);
        grid[1][1] = new Tile(Type.CAT);
        grid[2][1] = new Tile(Type.CAT);
        grid[2][2] = new Tile(Type.CAT);
        grid[3][2] = new Tile(Type.CAT);


        grid[4][0] = new Tile(Type.GAME);
        grid[5][0] = new Tile(Type.GAME);
        grid[4][1] = new Tile(Type.GAME);
        grid[5][1] = new Tile(Type.GAME);

        assertEquals(11,shelf.getAdjScore());

    }

    @Test
    public void getAdjScore_6AdjacentCatAnd4AdjacentGamesNearEachOther_11PointsReturned(){
        Shelf shelf = new Shelf();
        Tile[][] grid = shelf.getGrid();

        grid[0][0] = new Tile(Type.CAT);
        grid[0][1] = new Tile(Type.CAT);
        grid[1][1] = new Tile(Type.CAT);
        grid[2][1] = new Tile(Type.CAT);
        grid[2][2] = new Tile(Type.CAT);
        grid[3][2] = new Tile(Type.CAT);


        grid[3][0] = new Tile(Type.GAME);
        grid[4][0] = new Tile(Type.GAME);
        grid[3][1] = new Tile(Type.GAME);
        grid[4][1] = new Tile(Type.GAME);

        assertEquals(11,shelf.getAdjScore());

    }

    @Test
    public void getAdjScore_4AdjacentCat2AdjPlatn3AdjFrame5AdjTrophy_10ptsReturned(){
        Shelf shelf = new Shelf();
        Tile[][] grid = shelf.getGrid();

        grid[0][0] = new Tile(Type.CAT);
        grid[0][1] = new Tile(Type.CAT);
        grid[1][1] = new Tile(Type.CAT);
        grid[2][1] = new Tile(Type.CAT);

        grid[1][2] = new Tile(Type.PLANT);
        grid[2][2] = new Tile(Type.PLANT);


        grid[0][3] = new Tile(Type.FRAME);
        grid[1][3] = new Tile(Type.FRAME);
        grid[2][3] = new Tile(Type.FRAME);

        grid[3][0] = new Tile(Type.TROPHY);
        grid[3][1] = new Tile(Type.TROPHY);
        grid[3][2] = new Tile(Type.TROPHY);
        grid[3][3] = new Tile(Type.TROPHY);
        grid[3][4] = new Tile(Type.TROPHY);


        assertEquals(10,shelf.getAdjScore());
    }

// insertTiles tests
    @Test
    public void insertTiles_Test(){
        Shelf s = new Shelf();
        List<Tile> tileList = new ArrayList<>();
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.FRAME));

        Tile[][] grid = s.getGrid();
        s.insertTiles(tileList, 0);
        assertEquals(grid[0][0].getType(), Type.CAT);
        assertEquals(grid[1][0].getType(), Type.BOOK);
        assertEquals(grid[2][0].getType(), Type.FRAME);
    }
    @Test
    public void insertTooManyTiles_Test(){
        Shelf s = new Shelf();

        List<Tile> tileList = new ArrayList<>();
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.FRAME));
        s.insertTiles(tileList, 0);
        tileList.clear();

        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.FRAME));
        s.insertTiles(tileList, 0);
        tileList.clear();

        /*
        C x x x x
        B x x x x
        F x x x x
        C x x x x
        B x x x x
        F x x x x
                */

        //System.out.println(s);
        boolean flag = false;
        tileList.add(new Tile(Type.CAT));
        try{
            s.insertTiles(tileList, 0);
        } catch (IndexOutOfBoundsException e){
            flag = true;
        }
        assertTrue(flag);
    }
    @Test
    public void insertTiles_BoundsTest(){
        Shelf s = new Shelf();
        //add 4 tiles
        List<Tile> tileList = new ArrayList<>();
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.FRAME));
        tileList.add(new Tile(Type.FRAME));
        boolean flag = false;
        try{
            s.insertTiles(tileList, 0);
        } catch (IndexOutOfBoundsException e){
            flag = true;
        }
        assertTrue(flag);

        flag = false;
        try{
            s.insertTiles(tileList, -1);
        } catch (IndexOutOfBoundsException e){
            flag = true;
        }
        assertTrue(flag);

        flag = false;
        try{
            s.insertTiles(tileList, 5);
        } catch (IndexOutOfBoundsException e){
            flag = true;
        }
        assertTrue(flag);
    }
//************
}
