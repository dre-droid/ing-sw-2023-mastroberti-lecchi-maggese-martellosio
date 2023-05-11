package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PlayerTest {
    Player player;
    List<Tile> list;
    Tile[][] grid;
    @BeforeEach
    void setUp(){
        Board board = new Board(2);
        player = new Player("test", false, board);
        //Bag bag = new Bag();
        list = new ArrayList<Tile>();
        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));
        player.setCurrentTiles(list);
        player.insertTiles(list, 3);
    }

    @Test
    @DisplayName("Test insertion of tiles in player's shelf")
    void testInsertTiles(){

        Tile[][] test = new Shelf().getGrid();
        test[1-1][4-1] = new Tile(Type.CAT);
        test[2-1][4-1] = new Tile(Type.CAT);
        test[3-1][4-1] = new Tile(Type.CAT);
        //check that every tile matches the test
        for (int rows = 0; rows < 6; rows++)
            for (int columns = 0; columns < 5; columns++){
                Tile t1 =test[rows][columns];
                Tile t2 = player.getShelf().getGrid()[rows][columns];
                if (Objects.isNull(t1) || Objects.isNull(t2)) assertEquals(Objects.isNull(t1), Objects.isNull(t2));
                else assertEquals(t1.toString(), t2.toString());
        }

        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));

        assertFalse(player.insertTiles(list, 3));

    }
    @Test
    void testInsertTilesFalse(){
        //if player tries to put 7 tiles in the same column of a shelf insertTiles returns false
        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));
        list.add(new Tile(Type.CAT));

        assertFalse(player.insertTiles(list, 3));

    }
/*
    @Test
    void checkPersonalGoalTest(){

        Game g;
        Player p1, p2;
        g = new Game(2);
        p1 = new Player("p1", true, null);
        p2 = new Player("p2", false, null);
        g.fillValidTileMap();
        g.addPlayer(p1.getNickname());
        g.addPlayer(p2.getNickname());
        p1.setPersonalGoalCard(g.getValidTilesMap().get(5));

        List<Tile> list2, list3, list4;
        list2 = new ArrayList<Tile>();
        list2.add(new Tile(Type.GAME));
        list2.add(new Tile(Type.GAME));
        p1.setCurrentTiles(list2);
        p1.insertTiles(list2, 0);
        list3 = new ArrayList<Tile>();
        list3.add(new Tile(Type.CAT));
        list3.add(new Tile(Type.CAT));
        p1.setCurrentTiles(list3);
        p1.insertTiles(list3, 3);
        list4 = new ArrayList<Tile>();
        list4.add(new Tile(Type.PLANT));
        list4.add(new Tile(Type.PLANT));
        p1.setCurrentTiles(list4);
        p1.insertTiles(list4, 4);
        System.out.println(p1.getShelf().toString());
        Shelf shelf;
        shelf = p1.getShelf();
        System.out.println(p1.getPersonalGoalCard().getPoints(shelf));

        assertEquals(4, p1.checkPersonalGoal());





        this works
        Shelf shelf = new Shelf();
        shelf.getGrid()[4][4]= new Tile(Type.PLANT);
        shelf.getGrid()[5][3]= new Tile(Type.CAT);
        shelf.getGrid()[5][0]= new Tile(Type.GAME);
        p1.setShelf(shelf);
        assertEquals(4, p1.checkPersonalGoal());


    }
    */

}
