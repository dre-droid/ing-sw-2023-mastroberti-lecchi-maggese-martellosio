package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;

import main.java.it.polimi.ingsw.Model.PersonalGoalCards.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersonalGoalCardTest {
    @Test
    public void getPoints_0itemsEqualToPGC1_return0(){
        Shelf shelf = new Shelf();
        Player p = new Player("Paolo", true, new Board());
        p.setPersonalGoalCard(new PersonalGoalCard1());
        assertEquals(0, p.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_1itemsEqualToPGC1_return1(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);

        Player p = new Player("Paolo", true, new Board());
        p.setPersonalGoalCard(new PersonalGoalCard1());
        assertEquals(1, p.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_2itemsEqualToPGC1_return2(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);

        Player p = new Player("Paolo", true, new Board());
        p.setPersonalGoalCard(new PersonalGoalCard1());
        assertEquals(2, p.getPersonalGoalCard().getPoints(shelf));
    }



    @Test
    public void getPoints_3itemsEqualToPGC1_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][2]= new Tile(Type.FRAME);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);

        Player p = new Player("Paolo", true, new Board());
        p.setPersonalGoalCard(new PersonalGoalCard1());
        assertEquals(4, p.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_4itemsEqualToPGC2_return6(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[5][4]= new Tile(Type.FRAME);
        shelf.getGrid()[2][0]= new Tile(Type.CAT);
        shelf.getGrid()[3][4]= new Tile(Type.BOOK);

        Player p = new Player("Paolo", true, new Board());
        p.setPersonalGoalCard(new PersonalGoalCard2());
        assertEquals(6, p.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_5itemsEqualToPGC12_return9(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[1][1]= new Tile(Type.PLANT);
        shelf.getGrid()[2][2]= new Tile(Type.FRAME);
        shelf.getGrid()[5][0]= new Tile(Type.CAT);
        shelf.getGrid()[0][2]= new Tile(Type.BOOK);
        shelf.getGrid()[3][3]= new Tile(Type.TROPHY);

        Player p = new Player("Paolo", true, new Board());
        p.setPersonalGoalCard(new PersonalGoalCard12());
        assertEquals(9, p.getPersonalGoalCard().getPoints(shelf));
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


        Player p = new Player("Paolo", true, new Board());
        p.setPersonalGoalCard(new PersonalGoalCard1());
        assertEquals(12, p.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void toStringTest(){
        PersonalGoalCard p1 = new PersonalGoalCard7();
        p1.initializeValidTiles();
        System.out.println(p1);
    }
}
