package GUI.PositionStuff;

public class Position {
    private int x;
    private int y;
    public Position(int x,int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }


    @Override
    public boolean equals(Object obj) {
        Position p = (Position) obj;
        return this.x == p.getX() && this.y == p.getY();
    }

    @Override
    public String toString(){
        return "Position: " + getX() + ", " + getY();
    }
}
