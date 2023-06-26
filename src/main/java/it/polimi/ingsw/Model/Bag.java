package main.java.it.polimi.ingsw.Model;

import java.util.*;

public class Bag {
    private List<Tile> tiles;

    public Bag(){
        this.tiles = new ArrayList<Tile>();
        for(Type type: Type.values()){
            for(int i = 0; i<22;i++){
                Tile tile = new Tile(type);
                tiles.add(tile);
            }
        }
    }

    /**
     * This method returns all the Tiles present in the bag
     * @return All the tiles present in the Bag
     */
    public List<Tile> getAllTiles(){
        List<Tile> allTiles = new ArrayList<Tile>();
        for(int i = 0;i<tiles.size();i++){
            allTiles.add(tiles.get(i));
        }
        return allTiles;
    }

    public int getSize(){
        return tiles.size();
    }

    /**
     * This method remove a random Tile from the Bag and returns it
     * @return a random Tile from the bag
     */
    public Tile pickRandomTile(){
        Random random = new Random();
        int randomIndex = random.nextInt(this.getSize());
        Tile t = tiles.get(randomIndex);
        tiles.remove(randomIndex);
        return t;
    }

    /**
     * This method reinserts a Tile inside the bag (at the end of the turn if the board needs to be refilled, all the isolated tiles
     * on the board are shuffled back into the Bag
     * @param t it's the tile that has to be reinserted into the Bag
     */
    public void reinsertTile(Tile t){
        this.tiles.add(t);
    }

}
