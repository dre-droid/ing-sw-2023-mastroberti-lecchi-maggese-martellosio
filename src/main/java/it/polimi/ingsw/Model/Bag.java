package main.java.it.polimi.ingsw.Model;

import java.util.*;

public class Bag {
    private List<Tile> tiles;

    public Bag(){
        for(Type type: Type.values()){
            for(int i = 0; i<7;i++){
                Tile tile = new Tile(type);
                tiles.add(tile);
            }
        }
    }

    public List<Tile> getAllTiles(){
        List<Tile> allTiles = new ArrayList<Tile>();
        for(int i = 0;i<tiles.size();i++){
            allTiles.add(tiles.get(i));
        }
        return allTiles;
    }
}
