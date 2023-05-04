package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

import java.util.Iterator;

public class GameTest{
    Game g;
    Player p1, p2, p3, p4;

    @Test
    void playTurnTest() throws Exception {
        g = new Game(2);
        p1 = new Player("p1", false, null);
        p2 = new Player("p2", false, null);
        g.addPlayer("p1");
        g.addPlayer("p2");

        g.getBoard().setTileCAT(1, 3);
        g.getBoard().setTileBOOK(1, 4);
        g.getBoard().setTileCAT(2, 3);
        g.getBoard().setTileBOOK(2, 4);
        g.getBoard().setTilePLANT(2, 5);

                /*
         ----- Board ------
        - 0 1 2 3 4 5 6 7 8
        0 X X X X X X X X X
        1 X X X C B X X X X
        2 X X X C B P X X X
        3 X X F P C G B G X
        4 X B T G F F F F X
        5 X T T G G F C X X
        6 X X X P B G X X X
        7 X X X X T G X X X
        8 X X X X X X X X X

                 */

        //g.getBoard().printGridMap();
        //check that drawn tiles are inserted correctly in players' shelves
        if (g.isPlaying.getNickname().equals("p1")){
            //g.playTurn(1, 3, 2, Board.Direction.RIGHT, 0,21);
            Tile t1 = p1.getShelf().getGrid()[0][0];
            Tile t2 = p1.getShelf().getGrid()[0][1];

            p1.getShelf().getGridForDisplay();

            Assert.assertEquals(t1.toString(), "B");
            Assert.assertEquals(t2.toString(), "C");

            //g.playTurn(2, 3, 3, Board.Direction.RIGHT, 0,132);
            t1 = p2.getShelf().getGrid()[0][0];
            t2 = p2.getShelf().getGrid()[0][1];
            Tile t3 = p2.getShelf().getGrid()[0][1];

            Assert.assertEquals(t1.toString(), "C");
            Assert.assertEquals(t2.toString(), "P");
            Assert.assertEquals(t2.toString(), "B");
        }
        else {
            //g.playTurn(1, 3, 2, Board.Direction.RIGHT, 0,21);
            Tile t1 = p2.getShelf().getGrid()[0][0];
            Tile t2 = p2.getShelf().getGrid()[0][1];

            Assert.assertEquals(t1.toString(), "B");
            Assert.assertEquals(t2.toString(), "C");

//            g.playTurn(2, 3, 3, Board.Direction.RIGHT, 0,132);
            t1 = p1.getShelf().getGrid()[0][0];
            t2 = p1.getShelf().getGrid()[0][1];
            Tile t3 = p1.getShelf().getGrid()[0][1];

            Assert.assertEquals(t1.toString(), "C");
            Assert.assertEquals(t2.toString(), "P");
            Assert.assertEquals(t2.toString(), "B");
        }



    }
    /*
    @BeforeEach
    void setUp(){
        g = new Game(4);
        p1 = new Player("p1", false, null);
        p2 = new Player("p2", false, null);
        p3 = new Player("p3", false, null);
        p4 = new Player("p4", false, null);
        g.addPlayer(p1.getNickname());
        g.addPlayer(p2.getNickname());
        g.addPlayer(p3.getNickname());
        g.addPlayer(p4.getNickname());
    }

    @Test
    void playTurnTest_4P() throws InvalidMoveException, Exception{
        g.gameStartSetup();
        Tile[][] test;
        TilePlacingSpot tile;

        //test = g.isPlaying.getShelf().getGrid();
        //test[0][0] = new Tile(Type.CAT);
        //test[0][1] = new Tile(Type.CAT);
        //test[0][2] = new Tile(Type.CAT);


        g.playTurn(0, 3, 2, Board.Direction.RIGHT, 0, 12);
        g.playTurn(1, 3, 3, Board.Direction.RIGHT, 0, 123);
        g.playTurn(2, 2, 3, Board.Direction.RIGHT, 0, 123);
        g.playTurn(2, 5, 2, Board.Direction.RIGHT, 0, 12);
        for (int i = 0; i < 3; i++) {
            for (int j = 2; j < 7; j++) {
                tile = g.getBoard().getBoardForDisplay()[i][j];
                if (i == 2 && (j == 2 || j == 6)){
                    org.junit.Assert.assertEquals(tile.isEmpty(), true);
                    org.junit.Assert.assertEquals(tile.isAvailable(), true);
                }
                if (i != 2 && (j == 2 || j == 6)) {
                    org.junit.Assert.assertEquals(tile.isEmpty(), false);
                    org.junit.Assert.assertEquals(tile.isAvailable(), true);
                }
                else {
                    org.junit.Assert.assertEquals(tile.isEmpty(), true);
                    org.junit.Assert.assertEquals(tile.isAvailable(), true);
                }
                Iterator<Player> iterator = g.getPlayerList().iterator();
                int count = 0;
                for (Player p: g.getPlayerList()){
                    if (iterator.next() == g.isPlaying){
                        do {
                            while (iterator.hasNext()) {
                                Assert.assertNotNull(p.getShelf().getGrid()[0][0]);
                                Assert.assertNotNull(p.getShelf().getGrid()[1][0]);
                                if (count == 1 || count == 2) Assert.assertNotNull(p.getShelf().getGrid()[0][2]);
                                count++;
                            }
                            iterator = g.getPlayerList().iterator();
                        }while (count < 4);
                    }
                    break;
                }
            }
        }
    }

    @Test
    void setFirstPlayer_Test(){
        int sum = 0;
        int bool;
        for (Player p: g.getPlayerList()){
            bool = p.getFirstPlayerSeat() ? 1 : 0;
            sum += bool;
        }
        Assert.assertEquals(sum, 1);

    }

    @Test
    void chooseCommonGoals_Test(){
        Assert.assertEquals(g.getCommonGoalCards().size(), 2);
    }
        */

}


