package main.java.it.polimi.ingsw.Model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {
    private Shelf shelf;
    private int score;
    private List<Tile> currentTiles;
    private List<ScoringToken> scoringTokensList;
    private String nickname;
    private PersonalGoalCard personalGoalCard;
    private boolean endGameToken;
    private boolean firstPlayerSeat;
    //private Board board;

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
     * encapsulates Board's drawTiles
     * @return the list of drawn tiles
     */
    /*public List<Tile> drawTiles(int x, int y, int amount, Board.Direction direction) throws InvalidMoveException{
        List<Tile> list = new ArrayList<>();
        list = board.drawTiles(x, y, amount, direction);
        return list;
    }*/

    /**
     * wrapper for insertTiles in Shelf
     * @author Andrea Mastroberti
     * @param currentTiles - list of tiles to be added to shelf, max length 3
     * @param column - column where tiles will be inserted, column values range [0 ... 4]
     */
    public boolean insertTiles(List<Tile> currentTiles, int column) {
        try {
            shelf.insertTiles(currentTiles, column);
            if (shelf.isFull()) setEndGameToken();
            return true;
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println(e);
            System.out.println("Choose another column.");
            return false;
        }
    }

    public int checkPersonalGoal(){
        return personalGoalCard.getPoints(shelf);
    }

    //setters
    /*public void setBoard(Board b){
        this.board = b;
    }*/
    public void setCurrentTiles(List<Tile> list){
        this.currentTiles = list;
    }
    private void setEndGameToken() {this.endGameToken = true;}
    public void setFirstPlayerSeat() {this.firstPlayerSeat = true;}
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
    }
    public void updateFinalScore(){
        score = getTokensScore() + shelf.getAdjScore() + checkPersonalGoal();
    }
    private int getTokensScore(){
        int sum = 0;
        if (scoringTokensList.isEmpty()) return 0;
        for (ScoringToken t : scoringTokensList) sum += t.points;
        return sum;
    }
    public void printShelf(){
        Tile[][] grid = shelf.getGrid();
        for (int i = 5; i >= 0; i--) {
            for (int j = 0; j < 5; j++){
                if (grid[i][j] == null) System.out.printf("X ");
                else System.out.printf("%s ", grid[i][j].toString());
            }
            System.out.println();
        }
        System.out.println();
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
