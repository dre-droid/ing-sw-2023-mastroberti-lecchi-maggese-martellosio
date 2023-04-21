package main.java.it.polimi.ingsw.Model;

import java.io.Serializable;

/**
 * class Tile represents a single tile
 */

public class Tile implements Serializable {
    private Type type;

    public Tile(){}
    public Tile(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString(){
        return this.type.name().substring(0, 1);
    }
}
