package test.java.it.polimi.ingsw.Model;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.Tile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest{

    public Board setCustomBoard(){
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
    public void playTurn2P_Test() throws Exception {
        ArrayList<Player> p = new ArrayList<>();
        Board board = setCustomBoard();
        Player p1 = new Player("p1", false, null);
        Player p2 = new Player("p2", false, null);
        p.add(p1);
        p.add(p2);
        Game g = new Game(2, board, p);

        TilePlacingSpot[][] grid = board.getBoardForDisplay();
        //check that board has been correctly set for testing
        assertEquals(grid[1][3].showTileInThisPosition().getType(), Type.CAT);
        assertEquals(grid[1][4].showTileInThisPosition().getType(), Type.BOOK);
        assertEquals(grid[3][3].showTileInThisPosition().getType(), Type.PLANT);
        assertEquals(grid[4][5].showTileInThisPosition().getType(), Type.CAT);
        assertEquals(grid[5][5].showTileInThisPosition().getType(), Type.CAT);
        assertEquals(grid[6][5].showTileInThisPosition().getType(), Type.CAT);

        ArrayList<Tile> tileList = new ArrayList<>();
        tileList.add(new Tile(Type.CAT));
        tileList.add(new Tile(Type.BOOK));
        g.playTurn(1, 3, 2, Board.Direction.RIGHT, 0, tileList);
        grid = g.getBoard().getBoardForDisplay();
        assertEquals(p2.getShelf().getGrid()[0][0].getType(), Type.CAT);
        assertEquals(p2.getShelf().getGrid()[1][0].getType(), Type.BOOK);
        assertEquals(grid[1][3].showTileInThisPosition(), null);
        assertEquals(grid[1][4].showTileInThisPosition(), null);
    }

/*
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

 */
}


