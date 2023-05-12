package test.java.it.polimi.ingsw.Model;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest{
    Game g;
    Player p1, p2;
    Board board;
    ArrayList<Player> p;

    @BeforeEach
    public void setUp(){
        p = new ArrayList<>();
        board = setCustomBoard();
        p1 = new Player("p1", false, null);
        p2 = new Player("p2", true, null);
        p.add(p1);
        p.add(p2);
        g = new Game(2, board, p);
    }

    /**
     * Tests playTurn with the following setup: the board is the one set and shown in setCustomBoard; 2 players, p1 and p2; commonGoalCards are diagonal
     * and four corners of the same type.
     */
    @Test
    public void playTurn2P_Test() throws Exception {
        System.out.println(g.getCommonGoalCards().get(0).getDescription());
        System.out.println(g.getCommonGoalCards().get(1).getDescription());

        //first turn - p2
        ArrayList<Tile> tileList = new ArrayList<>();
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        g.playTurn(1, 3, 2, Board.Direction.RIGHT, 0, tileList);
        TilePlacingSpot[][] grid = g.getBoard().getBoardForDisplay();
        assertEquals(p2.getShelf().getGrid()[5][0].getType(), Type.CAT);
        assertEquals(p2.getShelf().getGrid()[4][0].getType(), Type.BOOK);
        assertEquals(grid[1][3].showTileInThisPosition(), null);
        assertEquals(grid[1][4].showTileInThisPosition(), null);

        //second turn - p1
        tileList.add(new Tile(Type.PLANT));
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        g.playTurn(2, 3, 3, Board.Direction.RIGHT, 0, tileList);
        grid = g.getBoard().getBoardForDisplay();
        assertEquals(p1.getShelf().getGrid()[5][0].getType(), Type.PLANT);
        assertEquals(p1.getShelf().getGrid()[4][0].getType(), Type.CAT);
        assertEquals(p1.getShelf().getGrid()[3][0].getType(), Type.BOOK);
        assertEquals(grid[2][3].showTileInThisPosition(), null);
        assertEquals(grid[2][4].showTileInThisPosition(), null);
        assertEquals(grid[2][5].showTileInThisPosition(), null);

        //third turn p2
        tileList.add(new Tile(Type.PLANT));
        tileList.add(new Tile(Type.PLANT));
        tileList.add(new Tile(Type.CAT));
        g.playTurn(3, 2, 3, Board.Direction.RIGHT, 0, tileList);
        grid = g.getBoard().getBoardForDisplay();
        assertEquals(p2.getShelf().getGrid()[3][0].getType(), Type.PLANT);
        assertEquals(p2.getShelf().getGrid()[2][0].getType(), Type.PLANT);
        assertEquals(p2.getShelf().getGrid()[1][0].getType(), Type.CAT);
        assertEquals(grid[3][2].showTileInThisPosition(), null);
        assertEquals(grid[3][3].showTileInThisPosition(), null);
        assertEquals(grid[3][4].showTileInThisPosition(), null);

        //fourth turn - p1
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.BOOK));
        g.playTurn(3, 5, 3, Board.Direction.RIGHT, 0, tileList);
        grid = g.getBoard().getBoardForDisplay();
        assertEquals(p1.getShelf().getGrid()[2][0].getType(), Type.CAT);
        assertEquals(p1.getShelf().getGrid()[1][0].getType(), Type.BOOK);
        assertEquals(p1.getShelf().getGrid()[0][0].getType(), Type.BOOK);
        assertEquals(grid[3][5].showTileInThisPosition(), null);
        assertEquals(grid[3][6].showTileInThisPosition(), null);
        assertEquals(grid[3][7].showTileInThisPosition(), null);

        //sets players shelf such that common goals are fulfilled
        tileList.add(new Tile(Type.BOOK));
        p1.getShelf().insertTiles(tileList, 4);
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.BOOK));
        p1.getShelf().insertTiles(tileList, 3);
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.BOOK));
        p1.getShelf().insertTiles(tileList, 2);
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.BOOK));
        tileList.add(new Tile(Type.BOOK));
        p1.getShelf().insertTiles(tileList, 1);
        tileList.add(new Tile(Type.BOOK));
        p1.getShelf().insertTiles(tileList, 1);
        System.out.println("p1's shelf has been set as to complete the diagonal common goal card");
        System.out.println(p1.getShelf());

        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.CAT));
        p2.getShelf().insertTiles(tileList, 4);
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.CAT));
        p2.getShelf().insertTiles(tileList, 4);
        System.out.println("p2's shelf set such that common goal four corners of same type should be fulfilled");
        System.out.println(p2.getShelf());

        //fifth turn p2
        tileList.add(new Tile(Type.CAT));
        g.playTurn(4, 4, 1, Board.Direction.RIGHT, 0, tileList);
        grid = g.getBoard().getBoardForDisplay();
        assertEquals(p2.getShelf().getGrid()[0][0].getType(), Type.CAT);
        assertNull(grid[4][4].showTileInThisPosition());
        assertEquals(p2.getScoringTokensList().size(), 1);

        //sixth turn p1
        tileList.add(new Tile(Type.PLANT));
        g.playTurn(4, 1, 1, Board.Direction.RIGHT, 1, tileList);
        grid = g.getBoard().getBoardForDisplay();
        assertEquals(p1.getShelf().getGrid()[2][1].getType(), Type.BOOK);
        assertNull(grid[4][1].showTileInThisPosition());
        assertEquals(p1.getScoringTokensList().size(), 1);

    }
    private Board setCustomBoard(){
        Board board = new Board(2);
        board.setTileCAT(1, 3);
        board.setTileBOOK(1, 4);

        board.setTilePLANT(2, 3);
        board.setTileCAT(2, 4);
        board.setTileBOOK(2, 5);

        board.setTilePLANT(3, 2);
        board.setTilePLANT(3, 3);
        board.setTileCAT(3, 4);
        board.setTileCAT(3, 5);
        board.setTileBOOK(3, 6);
        board.setTileBOOK(3, 7);

        board.setTilePLANT(4, 1);
        board.setTilePLANT(4, 2);
        board.setTilePLANT(4, 3);
        board.setTileCAT(4, 4);
        board.setTileCAT(4, 5);
        board.setTileBOOK(4, 6);
        board.setTileBOOK(4, 7);

        board.setTilePLANT(5, 1);
        board.setTilePLANT(5, 2);
        board.setTilePLANT(5, 3);
        board.setTileCAT(5, 4);
        board.setTileCAT(5, 5);
        board.setTileBOOK(5, 6);

        board.setTilePLANT(6, 3);
        board.setTileBOOK(6, 4);
        board.setTileCAT(6, 5);

        board.setTileCAT(7, 4);
        board.setTileBOOK(7, 5);

        //  ----- Board ------
        //- 0 1 2 3 4 5 6 7 8
        //0 X X X X X X X X X
        //1 X X X C B X X X X
        //2 X X X P C B X X X
        //3 X X P P C C B B X
        //4 X P P P C C B B X
        //5 X P P P C C B X X
        //6 X X X P B C X X X
        //7 X X X X C G X X X
        //8 X X X X X X X X X

        return board;
    }

    @Test
    void setFirstPlayer_Test(){
        int sum = 0;
        int bool;
        for (Player p: g.getPlayerList()){
            bool = p.getFirstPlayerSeat() ? 1 : 0;
            sum += bool;
        }
        assertEquals(sum, 1);
    }

    @Test
    void chooseCommonGoals_Test(){
        assertEquals(g.getCommonGoalCards().size(), 2);
    }
}


