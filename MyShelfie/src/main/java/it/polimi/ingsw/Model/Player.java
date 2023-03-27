package main.java.it.polimi.ingsw.Model;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private Shelf shelf;
    private List<Tile> currentTiles;
    private List<ScoringToken> scoringTokensList;
    private String nickname;
    private PersonalGoalCard personalGoalCard;
    private boolean endGameToken;
    private boolean firstPlayerSeat;

    /**
     * @param personalGoalCard - unique personal goal card is assigned when player joins the game
     * @param firstPlayerSeat - bool, true if player is the first to play
     */
    public Player(String nickname, PersonalGoalCard personalGoalCard, boolean firstPlayerSeat){
        this.nickname = nickname;
        this.personalGoalCard = personalGoalCard;
        this.firstPlayerSeat = firstPlayerSeat;

        shelf = new Shelf();
        List<Tile> currentTiles = new ArrayList<Tile>();
        List<ScoringToken> scoringTokensList = new ArrayList<ScoringToken>();
        endGameToken = false;
    }

    //setters
    public void setCurrentTiles(List<Tile> list){
        this.currentTiles = list;
    }
    public void addScoringToken(ScoringToken t) {
        scoringTokensList.add(t);
    }

    /**
     *
     * @param currentTiles - list of tiles to be added to shelf, max length 3
     * @param column - column where tiles will be inserted, column values range [1 ... 5]
     */
    public void insertTiles(List<Tile> currentTiles, int column) {
        try {
            shelf.insertTiles(currentTiles, column - 1);
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println(e);
            System.out.println("Choose another column.");
        }
    }

    //getters
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
    public boolean hasEndGameToken(){
        return endGameToken;
    }

    /**
     * @author Andrea Mastroberti
     * computes total score, which is the sum of personal goal, common goal and adjacent tiles objectives
     */
    /*public int getScore(){
        return personalGoalCard.getPoints(shelf) + getTokensScore() + shelf.getAdjScore();
    }*/
    private int getTokensScore(){
        int sum = 0;
        for (ScoringToken t : scoringTokensList) sum += t.points;
        return sum;
    }

}
