package main.java.it.polimi.ingsw.Server.Socket;

import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.Tile;

import java.util.List;

public class drawInfo {
    int x, y, amount, column;
    Board.Direction direction;
    List<Tile> tiles;

    public drawInfo(){}

    public drawInfo(int x, int y, int amount, Board.Direction direction, int column, List<Tile> tiles){
        this.x = x;
        this.y = y;
        this.amount = amount;
        this.direction = direction;
        this.column = column;
        this.tiles = tiles;
    }

    //getters
    public int getColumn() {
        return column;
    }
    public int getAmount() {
        return amount;
    }
    public int getX() {
        return x;
    }
    public Board.Direction getDirection() {
        return direction;
    }
    public int getY() {
        return y;
    }
    public List<Tile> getTiles(){ return this.tiles;}

    //setters
    public void setColumn(int column) {
        this.column = column - 1;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public void setDirection(Board.Direction direction) {
        this.direction = direction;
    }
    public void setY(int y) {
        this.y = y;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setTiles(List<Tile> reorderedTiles) {
        this.tiles = reorderedTiles;
    }

    @Override
    public String toString(){
        return "x, y, amount, direction " + x + y +  amount + direction;
    }
}

