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
    //PGC1
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
    public void getPoints_4itemsEqualToPGC1_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][2]= new Tile(Type.FRAME);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);
        shelf.getGrid()[2][3]= new Tile(Type.BOOK);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC1_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);
        shelf.getGrid()[2][3]= new Tile(Type.BOOK);
        shelf.getGrid()[5][2]= new Tile(Type.TROPHY);
        shelf.getGrid()[0][2]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));
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


    //PGC2
    @Test
    public void getPoints_0itemsEqualToPGC2_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC2_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC2_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[2][0]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC2_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[2][0]= new Tile(Type.CAT);
        shelf.getGrid()[2][2]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC2_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[2][0]= new Tile(Type.CAT);
        shelf.getGrid()[2][2]= new Tile(Type.GAME);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC2_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[2][0]= new Tile(Type.CAT);
        shelf.getGrid()[2][2]= new Tile(Type.GAME);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);
        shelf.getGrid()[4][3]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC2_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[2][0]= new Tile(Type.CAT);
        shelf.getGrid()[2][2]= new Tile(Type.GAME);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);
        shelf.getGrid()[4][3]= new Tile(Type.TROPHY);
        shelf.getGrid()[5][4]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(2));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC3
    @Test
    public void getPoints_0itemsEqualToPGC3_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(3));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC3_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][2]= new Tile(Type.PLANT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(3));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC3_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][2]= new Tile(Type.PLANT);
        shelf.getGrid()[3][1]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(3));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC3_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][2]= new Tile(Type.PLANT);
        shelf.getGrid()[3][1]= new Tile(Type.CAT);
        shelf.getGrid()[1][3]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(3));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC3_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][2]= new Tile(Type.PLANT);
        shelf.getGrid()[3][1]= new Tile(Type.CAT);
        shelf.getGrid()[1][3]= new Tile(Type.GAME);
        shelf.getGrid()[5][0]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(3));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC3_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][2]= new Tile(Type.PLANT);
        shelf.getGrid()[3][1]= new Tile(Type.CAT);
        shelf.getGrid()[1][3]= new Tile(Type.GAME);
        shelf.getGrid()[5][0]= new Tile(Type.BOOK);
        shelf.getGrid()[3][4]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(3));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC3_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][2]= new Tile(Type.PLANT);
        shelf.getGrid()[3][1]= new Tile(Type.CAT);
        shelf.getGrid()[1][3]= new Tile(Type.GAME);
        shelf.getGrid()[5][0]= new Tile(Type.BOOK);
        shelf.getGrid()[3][4]= new Tile(Type.TROPHY);
        shelf.getGrid()[1][0]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(3));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }



    //PGC4
    @Test
    public void getPoints_0itemsEqualToPGC4_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(4));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC4_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][3]= new Tile(Type.PLANT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(4));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC4_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][3]= new Tile(Type.PLANT);
        shelf.getGrid()[4][2]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(4));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC4_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][3]= new Tile(Type.PLANT);
        shelf.getGrid()[4][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][4]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(4));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC4_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][3]= new Tile(Type.PLANT);
        shelf.getGrid()[4][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][4]= new Tile(Type.GAME);
        shelf.getGrid()[4][1]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(4));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC4_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][3]= new Tile(Type.PLANT);
        shelf.getGrid()[4][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][4]= new Tile(Type.GAME);
        shelf.getGrid()[4][1]= new Tile(Type.BOOK);
        shelf.getGrid()[2][0]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(4));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC4_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][3]= new Tile(Type.PLANT);
        shelf.getGrid()[4][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][4]= new Tile(Type.GAME);
        shelf.getGrid()[4][1]= new Tile(Type.BOOK);
        shelf.getGrid()[2][0]= new Tile(Type.TROPHY);
        shelf.getGrid()[2][2]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(4));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC5

    @Test
    public void getPoints_0itemsEqualToPGC5_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC5_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC5_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[5][3]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC5_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[5][3]= new Tile(Type.CAT);
        shelf.getGrid()[5][0]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC5_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[5][3]= new Tile(Type.CAT);
        shelf.getGrid()[5][0]= new Tile(Type.GAME);
        shelf.getGrid()[3][2]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC5_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[5][3]= new Tile(Type.CAT);
        shelf.getGrid()[5][0]= new Tile(Type.GAME);
        shelf.getGrid()[3][2]= new Tile(Type.BOOK);
        shelf.getGrid()[1][1]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC5_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[5][3]= new Tile(Type.CAT);
        shelf.getGrid()[5][0]= new Tile(Type.GAME);
        shelf.getGrid()[3][2]= new Tile(Type.BOOK);
        shelf.getGrid()[1][1]= new Tile(Type.TROPHY);
        shelf.getGrid()[3][1]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC6
    @Test
    public void getPoints_0itemsEqualToPGC6_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(6));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC6_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][0]= new Tile(Type.PLANT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(6));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC6_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][4]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(6));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC6_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][4]= new Tile(Type.CAT);
        shelf.getGrid()[4][1]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(6));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC6_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][4]= new Tile(Type.CAT);
        shelf.getGrid()[4][1]= new Tile(Type.GAME);
        shelf.getGrid()[2][3]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(6));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC6_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][4]= new Tile(Type.CAT);
        shelf.getGrid()[4][1]= new Tile(Type.GAME);
        shelf.getGrid()[2][3]= new Tile(Type.BOOK);
        shelf.getGrid()[0][2]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(6));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC6_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][4]= new Tile(Type.CAT);
        shelf.getGrid()[4][1]= new Tile(Type.GAME);
        shelf.getGrid()[2][3]= new Tile(Type.BOOK);
        shelf.getGrid()[0][2]= new Tile(Type.TROPHY);
        shelf.getGrid()[4][3]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(6));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC7

    @Test
    public void getPoints_0itemsEqualToPGC7_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(7));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC7_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][1]= new Tile(Type.PLANT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(7));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC7_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][1]= new Tile(Type.PLANT);
        shelf.getGrid()[0][0]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(7));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC7_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][1]= new Tile(Type.PLANT);
        shelf.getGrid()[0][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(7));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC7_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][1]= new Tile(Type.PLANT);
        shelf.getGrid()[0][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);
        shelf.getGrid()[5][2]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(7));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC7_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][1]= new Tile(Type.PLANT);
        shelf.getGrid()[0][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);
        shelf.getGrid()[5][2]= new Tile(Type.BOOK);
        shelf.getGrid()[3][0]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(7));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC7_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][1]= new Tile(Type.PLANT);
        shelf.getGrid()[0][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);
        shelf.getGrid()[5][2]= new Tile(Type.BOOK);
        shelf.getGrid()[3][0]= new Tile(Type.TROPHY);
        shelf.getGrid()[1][3]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(7));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC8

    @Test
    public void getPoints_0itemsEqualToPGC8_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(8));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC8_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][3]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(8));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC8_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][3]= new Tile(Type.GAME);
        shelf.getGrid()[4][3]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(8));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC8_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.CAT);
        shelf.getGrid()[5][3]= new Tile(Type.GAME);
        shelf.getGrid()[4][3]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(8));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC8_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][0]= new Tile(Type.PLANT);
        shelf.getGrid()[1][1]= new Tile(Type.CAT);
        shelf.getGrid()[5][3]= new Tile(Type.GAME);
        shelf.getGrid()[4][3]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(8));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC8_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][0]= new Tile(Type.PLANT);
        shelf.getGrid()[1][1]= new Tile(Type.CAT);
        shelf.getGrid()[5][3]= new Tile(Type.GAME);
        shelf.getGrid()[4][3]= new Tile(Type.BOOK);
        shelf.getGrid()[2][2]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(8));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC8_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][0]= new Tile(Type.PLANT);
        shelf.getGrid()[1][1]= new Tile(Type.CAT);
        shelf.getGrid()[5][3]= new Tile(Type.GAME);
        shelf.getGrid()[4][3]= new Tile(Type.BOOK);
        shelf.getGrid()[2][2]= new Tile(Type.TROPHY);
        shelf.getGrid()[0][4]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(8));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC9
    @Test
    public void getPoints_0itemsEqualToPGC9_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(9));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC9_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[2][2]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(9));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC9_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[2][2]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(9));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC9_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[2][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][2]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(9));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC9_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[2][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][2]= new Tile(Type.GAME);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(9));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC9_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[2][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][2]= new Tile(Type.GAME);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);
        shelf.getGrid()[4][1]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(9));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC9_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[2][2]= new Tile(Type.CAT);
        shelf.getGrid()[0][2]= new Tile(Type.GAME);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);
        shelf.getGrid()[4][1]= new Tile(Type.TROPHY);
        shelf.getGrid()[5][0]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(9));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC10

    @Test
    public void getPoints_0itemsEqualToPGC10_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(10));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC10_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[3][3]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(10));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC10_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][3]= new Tile(Type.PLANT);
        shelf.getGrid()[3][3]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(10));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC10_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][3]= new Tile(Type.PLANT);
        shelf.getGrid()[3][3]= new Tile(Type.CAT);
        shelf.getGrid()[1][1]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(10));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC10_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][3]= new Tile(Type.PLANT);
        shelf.getGrid()[3][3]= new Tile(Type.CAT);
        shelf.getGrid()[1][1]= new Tile(Type.GAME);
        shelf.getGrid()[2][0]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(10));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC10_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][3]= new Tile(Type.PLANT);
        shelf.getGrid()[3][3]= new Tile(Type.CAT);
        shelf.getGrid()[1][1]= new Tile(Type.GAME);
        shelf.getGrid()[2][0]= new Tile(Type.BOOK);
        shelf.getGrid()[0][4]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(10));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC10_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][3]= new Tile(Type.PLANT);
        shelf.getGrid()[3][3]= new Tile(Type.CAT);
        shelf.getGrid()[1][1]= new Tile(Type.GAME);
        shelf.getGrid()[2][0]= new Tile(Type.BOOK);
        shelf.getGrid()[0][4]= new Tile(Type.TROPHY);
        shelf.getGrid()[4][1]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(10));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }


    //PGC11

    @Test
    public void getPoints_0itemsEqualToPGC11_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(11));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC11_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(11));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC11_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][2]= new Tile(Type.PLANT);
        shelf.getGrid()[4][4]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(11));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC11_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][2]= new Tile(Type.PLANT);
        shelf.getGrid()[4][4]= new Tile(Type.CAT);
        shelf.getGrid()[2][0]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(11));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC11_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][2]= new Tile(Type.PLANT);
        shelf.getGrid()[4][4]= new Tile(Type.CAT);
        shelf.getGrid()[2][0]= new Tile(Type.GAME);
        shelf.getGrid()[1][1]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(11));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC11_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][2]= new Tile(Type.PLANT);
        shelf.getGrid()[4][4]= new Tile(Type.CAT);
        shelf.getGrid()[2][0]= new Tile(Type.GAME);
        shelf.getGrid()[1][1]= new Tile(Type.BOOK);
        shelf.getGrid()[5][3]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(11));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC11_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][2]= new Tile(Type.PLANT);
        shelf.getGrid()[4][4]= new Tile(Type.CAT);
        shelf.getGrid()[2][0]= new Tile(Type.GAME);
        shelf.getGrid()[1][1]= new Tile(Type.BOOK);
        shelf.getGrid()[5][3]= new Tile(Type.TROPHY);
        shelf.getGrid()[3][2]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(11));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }

    //PGC12
    @Test
    public void getPoints_0itemsEqualToPGC12_return0(){
        Shelf shelf = new Shelf();
        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(0, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC12_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[5][0]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(1, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC12_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[5][0]= new Tile(Type.CAT);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(2, p1.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC12_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[5][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(4, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC12_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[5][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);
        shelf.getGrid()[0][2]= new Tile(Type.BOOK);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(6, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC12_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[5][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);
        shelf.getGrid()[0][2]= new Tile(Type.BOOK);
        shelf.getGrid()[3][3]= new Tile(Type.TROPHY);


        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(9, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_6itemsEqualToPGC12_return12(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[5][0]= new Tile(Type.CAT);
        shelf.getGrid()[4][4]= new Tile(Type.GAME);
        shelf.getGrid()[0][2]= new Tile(Type.BOOK);
        shelf.getGrid()[3][3]= new Tile(Type.TROPHY);
        shelf.getGrid()[2][2]= new Tile(Type.FRAME);

        p1.setPersonalGoalCard(g.getValidTilesMap().get(12));
        assertEquals(12, p1.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void toStringTest(){
        p1.setPersonalGoalCard(g.getValidTilesMap().get(1));

        System.out.println(p1);
    }

}
