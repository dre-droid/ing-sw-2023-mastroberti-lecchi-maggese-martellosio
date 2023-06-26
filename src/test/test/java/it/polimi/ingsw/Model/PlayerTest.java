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
        test[5][3] = new Tile(Type.CAT);
        test[4][3] = new Tile(Type.CAT);
        test[3][3] = new Tile(Type.CAT);
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

    @Test
    void checkPersonalGoalTest(){

        Game g;
        g = new Game(2);
        g.addPlayer("p1");
        g.addPlayer("p2");
        g.getPlayerList().get(0).setPersonalGoalCard(g.getValidTilesMap().get(5));
        g.getPlayerList().get(1).setPersonalGoalCard(g.getValidTilesMap().get(1));

        List<Tile> list2, list3, list4;
        list2 = new ArrayList<Tile>();
        list2.add(new Tile(Type.GAME));
        list2.add(new Tile(Type.GAME));
        g.getPlayerList().get(0).setCurrentTiles(list2);
        g.getPlayerList().get(0).insertTiles(list2, 0);
        list3 = new ArrayList<Tile>();
        list3.add(new Tile(Type.CAT));
        list3.add(new Tile(Type.CAT));
        g.getPlayerList().get(0).setCurrentTiles(list3);
        g.getPlayerList().get(0).insertTiles(list3, 3);
        list4 = new ArrayList<Tile>();
        list4.add(new Tile(Type.PLANT));
        list4.add(new Tile(Type.PLANT));
        g.getPlayerList().get(0).setCurrentTiles(list4);
        g.getPlayerList().get(0).insertTiles(list4, 4);
        System.out.println(g.getPlayerList().get(0).getShelf().toString());


        assertEquals(4, g.getPlayerList().get(0).checkPersonalGoal());

    }


}
