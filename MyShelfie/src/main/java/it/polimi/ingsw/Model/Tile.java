package main.java.it.polimi.ingsw.Model;

import java.io.Serializable;
import java.util.Random;

/**
 * class Tile represents a single tile
 */

public class Tile implements Serializable {
    private Type type;
    private String imgPath;

    public Tile(){}
    public Tile(Type type) {
        this.type = type;
        imgPath = setImgPath();
    }

    public Type getType() {
        return type;
    }

    /**
     * @return the path to a random image between the three available of the same type
     */
    private String setImgPath() {
        Random random = new Random();
        int val = random.nextInt(3) + 1;

        switch (type) {
            case CAT -> {
                return "item_tiles/Gatti1." + val + ".png";
            }
            case BOOK -> {
                return "item_tiles/Libri1." + val + ".png";
            }
            case GAME -> {
                return "item_tiles/Giochi1." + val + ".png";
            }
            case FRAME -> {
                return "item_tiles/Cornici1." + val + ".png";
            }
            case TROPHY -> {
                return "item_tiles/Trofei1." + val + ".png";
            }
            case PLANT -> {
                return "item_tiles/Piante1." + val + ".png";
            }
        }
        return "";
    }

    public String getImgPath(){
        return imgPath;
    }

    @Override
    public String toString(){
        return this.type.name().substring(0, 1);
    }
}
