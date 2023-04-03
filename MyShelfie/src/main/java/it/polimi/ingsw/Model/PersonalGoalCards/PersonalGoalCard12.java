package main.java.it.polimi.ingsw.Model.PersonalGoalCards;

import main.java.it.polimi.ingsw.Model.PersonalGoalCard;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.Type;

public class PersonalGoalCard12 extends PersonalGoalCard {

    @Override
    public void initializeValidTiles() {
        this.validTiles.getGrid()[1][1] = new Tile(Type.PLANT);
        this.validTiles.getGrid()[5][0] = new Tile(Type.CAT);
        this.validTiles.getGrid()[4][4] = new Tile(Type.GAME);
        this.validTiles.getGrid()[0][2] = new Tile(Type.BOOK);
        this.validTiles.getGrid()[3][3] = new Tile(Type.TROPHY);
        this.validTiles.getGrid()[2][2] = new Tile(Type.FRAME);

    }
}