package Server.Socket;

import main.java.it.polimi.ingsw.Model.Board;

public class drawInfo {
    int x, y, amount, column, order;
    Board.Direction direction;

    public drawInfo(){}

    public drawInfo(int x, int y, int amount, Board.Direction direction, int column, int order){
        this.x = x;
        this.y = y;
        this.amount = amount;
        this.direction = direction;
        this.column = column;
        this.order = order;
    }

    //getters and setters
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
    public int getOrder(){return order;}

    public void setColumn(int column) {
        this.column = column;
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

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString(){
        return "x, y, amount, direction " + x + y +  amount + direction;
    }
}

