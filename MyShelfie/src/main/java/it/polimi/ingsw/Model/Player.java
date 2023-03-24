package main.java.it.polimi.ingsw.Model;
import java.util.List;

public class Player {
    private Shelf shelf;
    private int score;
    private List<Tile> currentTiles;
    private String nickname;
    private PersonalGoalCard personalGoalCard;
    private boolean endGameToken;
    private boolean firstPlayerSeat;

    public Shelf getShelf() {
        return shelf;
    }

    public PersonalGoalCard getPersonalGoalCard() {
        return personalGoalCard;
    }
    public String getNickname(){
        return nickname;
    }
    public boolean hasFirstPlayerSeat(){
        return firstPlayerSeat;
    }
    public int getScore(){
        return score;
    }


}
