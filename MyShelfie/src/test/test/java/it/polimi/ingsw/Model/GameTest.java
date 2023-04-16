package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

public class GameTest {
    Game g;
    Player p1, p2, p3, p4;

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
    void playTurnTest_4P() throws InvalidMoveException{
        Tile[][] test;
        test = g.isPlaying.getShelf().getGrid();
        test[0][0] = new Tile(Type.CAT);
        test[0][1] = new Tile(Type.CAT);
        test[0][2] = new Tile(Type.CAT);

        g.playTurn(0, 3, 2, Board.Direction.RIGHT, 0);
        g.playTurn(1, 3, 3, Board.Direction.RIGHT, 0);
        g.playTurn(2, 2, 3, Board.Direction.RIGHT, 0);
        g.playTurn(2, 6, 2, Board.Direction.LEFT, 0);
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
}

