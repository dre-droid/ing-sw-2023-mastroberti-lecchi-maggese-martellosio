package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.PersonalGoalCard1;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        player.insertTiles(list, 4);
    }

    @Test
    @DisplayName("Test insertion of tiles in player's shelf")
    void testInsertTiles(){
        /*for (int i = 0; i < 6; i++){
            for (int j = 0; j < 5; j++)
                System.out.println(grid[i][j]);
            System.out.println();
        }*/

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
    }
}
