package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {
    Game g;
    Board b;

    @BeforeEach
    void setUp(){
        Game g = new Game();
    }

    @Test
    void playTurnTest_4P(){
        Player p1 = new Player("p1", new PersonalGoalCard(), false, b);
        Player p2 = new Player("p2", new PersonalGoalCard(), false, b);
        Player p3 = new Player("p3", new PersonalGoalCard(), false, b);
        Player p4 = new Player("p4", new PersonalGoalCard(), false, b);
        g.addPlayer(p1.getNickname());
        g.addPlayer(p2.getNickname());
        g.addPlayer(p3.getNickname());
        g.addPlayer(p4.getNickname());
        g.gameStartSetup();

        g.playTurn(0, 0, 3, Board.Direction.RIGHT, 0);
        g.playTurn(1, 0, 3, Board.Direction.RIGHT, 0);
        g.playTurn(2, 0, 3, Board.Direction.RIGHT, 0);
        g.playTurn(3, 0, 3, Board.Direction.RIGHT, 0);





    }
}
