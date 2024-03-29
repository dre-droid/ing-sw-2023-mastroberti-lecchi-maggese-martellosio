package main.java.it.polimi.ingsw.Model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player implements Serializable{
    private Shelf shelf;
    private int score;
    private List<Tile> currentTiles;
    private List<ScoringToken> scoringTokensList;
    private String nickname;
    private PersonalGoalCard personalGoalCard;
    private boolean endGameToken;
    private boolean firstPlayerSeat;

    /**
     * @param firstPlayerSeat - bool, true if player is the first to play
     * @param board - the board the player's playing on
     */
    public Player(String nickname, boolean firstPlayerSeat, Board board){
        this.nickname = nickname;
        //this.board = board;
        this.score = 0;
        this.shelf = new Shelf();
        this.currentTiles = new ArrayList<Tile>();
        this.scoringTokensList = new ArrayList<ScoringToken>();
        this.firstPlayerSeat = firstPlayerSeat;
        this.endGameToken = false;
    }
    public Player(){

    }



    /**
     * wrapper for insertTiles in Shelf
     * @author Andrea Mastroberti
     * @param currentTiles - list of tiles to be added to shelf, max length 3
     * @param column - column where tiles will be inserted, column values range [0 ... 4]
     */
    public boolean insertTiles(List<Tile> currentTiles, int column) {
        try {
            System.out.println("player--> insert tiles called");
            shelf.insertTiles(currentTiles, column);
            //if (shelf.isFull()) setEndGameToken();
            return true;
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println(e);
            System.out.println("Choose another column.");
            return false;
        }
    }

    /**
     * This method calculates the points coming from the completion of the PersonalGoalCard
     * @return : the points obtained through completion of the PersonalGoalCard
     */
    public int checkPersonalGoal(){
        return personalGoalCard.getPoints(shelf);
    }
    public void setCurrentTiles(List<Tile> list){ this.currentTiles = list;
    }
    public void setEndGameToken() {this.endGameToken = true;}
    public void setFirstPlayerSeat() {this.firstPlayerSeat = true;}

    /**
     * This method adds ScoringToken t to the scoringTokenList
     * @param t: Scoring Token to be addes
     */
    public void addScoringToken(ScoringToken t) {
        scoringTokensList.add(t);
    }

    public void setPersonalGoalCard(PersonalGoalCard personalGoalCard) {
        this.personalGoalCard = personalGoalCard;
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
    public List<ScoringToken> getScoringTokensList(){
        if (!scoringTokensList.isEmpty()) return new ArrayList<>(scoringTokensList);
        else return null;
    }

    /**
     * @author Andrea Mastroberti
     * computes partial score, which is the sum of common goal and adjacent tiles objectives
     * (personal goal is computed at the end of the game)
     */
    public void updateScore(){
        score = getTokensScore() + shelf.getAdjScore();
        if(hasEndGameToken())
            score=score+1;
    }

    /**
     * This method is called at the end of the game to calculate the score updated
     * with the points coming from the PersonalGoal Cards (calculated in checkPersonalgoal())
     */
    public void updateFinalScore(){
        score = getTokensScore() + shelf.getAdjScore() + checkPersonalGoal();
        if(hasEndGameToken())
            score=score+1;
    }

    /**
     * This method is called to calculate CommonGoalCards points
     * @return : the points obtaine through the completion of CommonGoal Cards
     */
    private int getTokensScore(){
        int sum = 0;
        if (scoringTokensList.isEmpty()) return 0;
        for (ScoringToken t : scoringTokensList) sum += t.getPoints();
        return sum;
    }

    public boolean getFirstPlayerSeat(){
        return firstPlayerSeat;
    }
    public int getScore(){
        return score;
    }

    @Override
    public String toString(){
        return this.nickname;
    }
}
