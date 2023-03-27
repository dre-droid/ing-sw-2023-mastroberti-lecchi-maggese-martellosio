package test.test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.Bag;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.Type;
import org.junit.jupiter.api.Test;  //andrea mastroberti - inserito perche non funizonava sulla mia macchina. mettiamoci d'accordo
//import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BagTest {
    @Test
    public void BagConstructor_ListNotNull(){
        Bag bag = new Bag();
        assertNotNull(bag);
    }

    @Test
    public void BagConstructor_Contains132Tiles(){
        Bag bag = new Bag();
        assertEquals(132, bag.getAllTiles().size());
    }
    @Test
    public void BagConstructor_Contains22OfEachTileType(){
        Bag bag = new Bag();
        boolean problem=false;
        for(Type type: Type.values()){
            if(bag.getAllTiles().stream().filter(t->t.getType()==type).count()!=22)
                problem = true;
        }
        assertFalse(problem);
    }

    @Test
    public void PickRandomTile_TileReturnedIsNotNull(){
        Bag bag = new Bag();
        assertNotNull(bag.pickRandomTile());
    }
    @Test
    public void PickRandomTile_BagSizeHasDecreasedByOne(){
        Bag bag = new Bag();
        int sizeBefore = bag.getSize();
        bag.pickRandomTile();
        int sizeAfter = bag.getSize();
        assertEquals(sizeBefore-1,sizeAfter);
    }

    @Test
    public void ReinsertTile_TileIsInsertedCorrectly(){
        Bag bag = new Bag();
        Tile tile = new Tile(Type.CAT);
        int numOfCatTilesBefore = (int) bag.getAllTiles().stream().filter(t->t.getType()==Type.CAT).count();
        bag.reinsertTile(tile);
        int numOfCatTilesAfter = (int) bag.getAllTiles().stream().filter(t->t.getType()==Type.CAT).count();
        assertEquals(numOfCatTilesBefore+1,numOfCatTilesAfter);
    }
}