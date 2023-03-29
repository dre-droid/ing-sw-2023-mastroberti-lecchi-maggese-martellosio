package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {
    Game g;
    @BeforeEach
    void setUp(){
    }

    @Test
    void playTurnTest_4P(){
        g = new Game();
        Player p1 = new Player("p1", new PersonalGoalCard(), false, null);
        Player p2 = new Player("p2", new PersonalGoalCard(), false, null);
        Player p3 = new Player("p3", new PersonalGoalCard(), false, null);
        Player p4 = new Player("p4", new PersonalGoalCard(), false, null);
        g.addPlayer(p1.getNickname());
        g.addPlayer(p2.getNickname());
        g.addPlayer(p3.getNickname());
        g.addPlayer(p4.getNickname());
        g.gameStartSetup();

        (g.getBoard()).printGridMap();
        /*
        g.playTurn(0, 3, 2, Board.Direction.RIGHT, 0);  //this line indexes a matrix out of bounds
        g.getLeaderBoard();
        g.playTurn(1, 3, 3, Board.Direction.RIGHT, 0);
        g.getLeaderBoard();
        g.playTurn(2, 2, 3, Board.Direction.RIGHT, 0);
        g.getLeaderBoard();
        g.playTurn(2, 6, 2, Board.Direction.LEFT, 0);
        g.getLeaderBoard();
        */

    }
}
