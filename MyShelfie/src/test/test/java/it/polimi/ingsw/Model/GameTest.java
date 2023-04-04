package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard1;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard2;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard3;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

public class GameTest {
    Game g;
    Player p1;
    Player p2;
    Player p3;
    Player p4;
    @BeforeEach
    void setUp(){
        g = new Game();
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
    void playTurnTest_4P(){
        g.gameStartSetup();
        g.getBoard().printGridMap();
        System.out.println();

        g.playTurn(0, 3, 2, Board.Direction.RIGHT, 0);
        g.getBoard().printGridMap();
        g.getLeaderBoard();

        g.playTurn(1, 3, 3, Board.Direction.RIGHT, 0);
        g.getBoard().printGridMap();
        g.getLeaderBoard();

        g.playTurn(2, 2, 3, Board.Direction.RIGHT, 0);
        g.getBoard().printGridMap();
        g.getLeaderBoard();

        g.playTurn(2, 6, 2, Board.Direction.LEFT, 0);
        g.getBoard().printGridMap();
        g.getLeaderBoard();

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
}

