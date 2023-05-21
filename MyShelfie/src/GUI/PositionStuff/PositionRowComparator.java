package GUI.PositionStuff;

public class PositionRowComparator implements java.util.Comparator<Position>{
    @Override
    public int compare(Position p1, Position p2) {
        return p1.getX()-p2.getX();
    }
}
