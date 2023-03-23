package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.Bag;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

class BagTest {
    @Test
    void BagConstructor_ListNotNull(){
        Bag bag = new Bag();
        assertNotNull(bag);
    }
}