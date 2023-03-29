package test.java.it.polimi.ingsw.Model;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.Tile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

}
