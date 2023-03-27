package main.java.it.polimi.ingsw.Model;

public class Tile {
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
        return this.type.name();
    }
}
