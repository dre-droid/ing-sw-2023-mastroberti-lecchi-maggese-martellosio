package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard1;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersonalGoalCardTest {
    @Test
    public void getPoints_0itemsEqualToPGC1_return0(){
        Shelf shelf = new Shelf();
        Player p = new Player("Paolo", new PersonalGoalCard1(), true, new Board());
        assertEquals(0, p.getPersonalGoalCard().getPoints(shelf));
    }

    @Test
    public void getPoints_3itemsEqualToPGC1_return4(){
        Shelf shelf = new Shelf();
        shelf.getGrid()[0][0]= new Tile(Type.PLANT);
        shelf.getGrid()[0][2]= new Tile(Type.FRAME);
        shelf.getGrid()[1][4]= new Tile(Type.CAT);

        Player p = new Player("Paolo", new PersonalGoalCard1(), true, new Board());
        assertEquals(4, p.getPersonalGoalCard().getPoints(shelf));
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


        Player p = new Player("Paolo", new PersonalGoalCard1(), true, new Board());
        assertEquals(12, p.getPersonalGoalCard().getPoints(shelf));
    }

}
