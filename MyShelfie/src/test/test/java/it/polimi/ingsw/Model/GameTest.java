package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard1;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard2;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard3;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {
    Game g;
    @BeforeEach
    void setUp(){
    }
/*
    @Test
    void playTurnTest_4P(){
        g = new Game();
        Player p1 = new Player("p1", new PersonalGoalCard1(), false, null);
        Player p2 = new Player("p2", new PersonalGoalCard2(), false, null);
        Player p3 = new Player("p3", new PersonalGoalCard3(), false, null);
        Player p4 = new Player("p4", new PersonalGoalCard4(), false, null);
        g.addPlayer(p1.getNickname());
        g.addPlayer(p2.getNickname());
        g.addPlayer(p3.getNickname());
        g.addPlayer(p4.getNickname());
        g.gameStartSetup();

        g.getBoard().printGridMap();
        System.out.println();

        g.playTurn(0, 3, 2, Board.Direction.RIGHT, 0);
        g.isPlaying.printShelf();
        g.getBoard().printGridMap();
        g.getLeaderBoard();

        g.playTurn(1, 3, 3, Board.Direction.RIGHT, 0);
        g.isPlaying.printShelf();
        g.getBoard().printGridMap();
        g.getLeaderBoard();

        g.playTurn(2, 2, 3, Board.Direction.RIGHT, 0);
        g.isPlaying.printShelf();
        g.getBoard().printGridMap();
        g.getLeaderBoard();

        g.playTurn(2, 6, 2, Board.Direction.LEFT, 0);
        g.isPlaying.printShelf();
        g.getBoard().printGridMap();
        g.getLeaderBoard();



    }

 */
}

