package main.java.it.polimi.ingsw.Model;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private Shelf shelf;
    public int score;
    private List<Tile> currentTiles;
    private List<ScoringToken> scoringTokensList;
    private String nickname;
    private PersonalGoalCard personalGoalCard;
    private boolean endGameToken;
    private boolean firstPlayerSeat;
    private Board board;

    /**
     * @param personalGoalCard - unique personal goal card is assigned when player joins the game
     * @param firstPlayerSeat - bool, true if player is the first to play
     * @param board - the board the player's playing on
     */
    public Player(String nickname, PersonalGoalCard personalGoalCard, boolean firstPlayerSeat, Board board){
        this.nickname = nickname;
        this.personalGoalCard = personalGoalCard;
        this.firstPlayerSeat = firstPlayerSeat;
        this.board = board;
        shelf = new Shelf();
        List<Tile> currentTiles = new ArrayList<Tile>();
        List<ScoringToken> scoringTokensList = new ArrayList<ScoringToken>();
        endGameToken = false;
    }

    /**
     * encapsulates Board's drawTiles
     * @return the list of drawn tiles
     */
    public List<Tile> drawTiles(int x, int y, int amount, Board.Direction direction) {
        List<Tile> list = new ArrayList<>();
        try {
            list = board.drawTiles(x, y, amount, direction);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @author Andrea Mastroberti
     * wrapper for insertTiles in Shelf
     * @param currentTiles - list of tiles to be added to shelf, max length 3
     * @param column - column where tiles will be inserted, column values range [1 ... 5]
     */
    public void insertTiles(List<Tile> currentTiles, int column) {
        try {
            shelf.insertTiles(currentTiles, column - 1);
            if (shelf.isFull()) setEndGameToken();
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println(e);
            System.out.println("Choose another column.");
        }
    }

    public int checkPersonalGoal(){
        return personalGoalCard.getPoints(shelf);
    }

    //setters
    public void setCurrentTiles(List<Tile> list){
        this.currentTiles = list;
    }
    private void setEndGameToken() {this.endGameToken = true;}
    public void setFirstPlayerSeat() {this.firstPlayerSeat = true;}
    public void addScoringToken(ScoringToken t) {
        scoringTokensList.add(t);
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
     * computes partial score, which is the sum of common goal and adjacent tiles objectives
     * (personal goal is computed at the end of the game)
     */
    public void updateScore(){
        this.score = getTokensScore() + shelf.getAdjScore();
    }
    public void updateFinalScore(){
        score = getTokensScore() + shelf.getAdjScore() + checkPersonalGoal();
    }
    private int getTokensScore(){
        int sum = 0;
        for (ScoringToken t : scoringTokensList) sum += t.points;
        return sum;
    }

}
