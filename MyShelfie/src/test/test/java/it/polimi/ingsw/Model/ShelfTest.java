package test.test.java.it.polimi.ingsw.Model;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.Tile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShelfTest {

    @Test
    @DisplayName("Test for fun")
    public void firstTest(){
        assert 1 + 1 == 2;
    }

    @Test
    public void isFull_emptyShelf_returnFalse() {
         Shelf shelf = new Shelf();
         assertFalse(shelf.isFull());
    }
}
