package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersonalGoalCardTest {
    Game g;
    Player p1, p2;
    @BeforeEach
    void setUp(){
        g = new Game(2);
        p1 = new Player("p1", true, null);
        p2 = new Player("p2", false, null);
        g.fillValidTileMap();
        g.addPlayer(p1.getNickname());
        g.addPlayer(p2.getNickname());


    }
    @Test
    public void getPoints_0itemsEqualToPGC1_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC1_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC1_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC1_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][2]= new Tile(Type.FRAME);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC2_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[5][4]= new Tile(Type.FRAME);
        shelf.getGrid()[2][0]= new Tile(Type.CAT);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC12_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[2][2]= new Tile(Type.FRAME);
        shelf.getGrid()[5][0]= new Tile(Type.CAT);
        shelf.getGrid()[0][2]= new Tile(Type.BOOK);
        shelf.getGrid()[3][3]= new Tile(Type.TROPHY);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC1_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][2]= new Tile(Type.FRAME);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);
        shelf.getGrid()[2][3]= new Tile(Type.BOOK);
        shelf.getGrid()[3][1]= new Tile(Type.GAME);
        shelf.getGrid()[5][2]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void toStringTest(){
        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));

        System.out.println(p1);
    }


}
