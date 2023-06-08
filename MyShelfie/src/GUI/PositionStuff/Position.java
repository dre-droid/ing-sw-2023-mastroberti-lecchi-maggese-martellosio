package GUI.PositionStuff;

import java.util.Comparator;
import java.util.List;

public class Position {
    private int x;
    private int y;
    public Position(int x,int y){
        this.x = x;
        this.y = y;
    }

    /**
     * x is vertical axis, y is horizontal
     */
    public int getX(){
        return x;
    }

    /**
     * x is vertical axis, y is horizontal
     */
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
        return "Position: (" + getX() + ", " + getY()+")";
    }

    /**
     * This method is used to order a List of position, with ascending Y if the positions have the same X, with ascending X if the positions
     * have the same Y
     * @param positions list of positions that are going to be sorted
     * @return the sorted list if the positions all have the same X or the same Y, null otherwise
     */
    public static List<Position> sortPositions(List<Position> positions){
        //check same row or same column
        if(Position.posWithSameX(positions)) {
            //sort the list in order to have tile with x asc
            return positions.stream().sorted(Comparator.comparing(Position::getY)).toList();
        }else if(Position.posWithSameY(positions)){
            //sort the list in order to have tile with y asc
            return positions.stream().sorted(Comparator.comparing(Position::getX)).toList();
        }else{
            return null;
        }
    }

    /**
     * This method is used to check if the positions in the list all have the same X coordinate
     * @param positions list of positions
     * @return true if the positions all have the same X, false otherwise
     */
    public static boolean posWithSameX(List<Position> positions){
        return positions.stream().noneMatch(p->p.getX()!=positions.get(0).getX());
    }

    /**
     * This method is used to check if the positions in the list all have the same Y coordinate
     * @param positions list of positions
     * @return true if the positions all have the same Y, false otherwise
     */
    public static boolean posWithSameY(List<Position> positions){
        return positions.stream().noneMatch(p->p.getY()!=positions.get(0).getY());
    }
}
