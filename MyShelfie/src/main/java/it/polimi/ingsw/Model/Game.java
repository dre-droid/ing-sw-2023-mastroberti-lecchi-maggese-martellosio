package main.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.*;

import java.sql.Array;
import java.util.*;

public class Game {
    Player isPlaying;
    LinkedList<Player> playersList;
    List<Player> leaderBoard;
    Iterator<Player> iterator;
    List<CommonGoalCard> commonGoalCards;
    Board board;
    boolean lastTurn;
    boolean lastRound;


    /**
     * Constructor - creates a new instance of a game
     */
    public Game(){
        playersList = new LinkedList<>();
        leaderBoard = new ArrayList<>();
        commonGoalCards = new ArrayList<>();
        lastTurn = false;
        lastRound = false;
    }

    /**
     * after players have been added to the lobby,
     * game starts: sets first player, fills the board and chooses the common goal cards
     */
    public void gameStartSetup(){
       setFirstPlayer();
       setBoard();
       chooseCommonGoals();
    }

    /**
     * makes the player draw from the board and inserts tile in the shelf, then changes the isPlaying Player
     * parameters to call drawTiles and insertTiles methods in class Player
     */
    public void playTurn(int x, int y, int amount, Board.Direction direction, int column){
        //player draws from board and inserts in his shelf - is the shelf is full sets lastTurnFlag
        isPlaying.insertTiles(isPlaying.drawTiles(x, y, amount, direction), column);
        if (isPlaying.hasEndGameToken()) setLastTurnFlag();

        //check common goal and eventually give player token
        for (CommonGoalCard c: commonGoalCards)
            if (c.isSatisfiedBy(isPlaying)) {
                try {
                    isPlaying.addScoringToken(c.getReward(isPlaying));
                }
                catch (CannotCollectRewardException e) {
                    e.printStackTrace();
                }
            }

        //refill board if necessary
        board.refill();

        //update score and leaderboard
        isPlaying.updateScore();
        Collections.sort(leaderBoard, new scoreComparator());

        //next turn and end game logic
        Player nextPlayer = iterator.next();
        if (isPlaying.hasEndGameToken()) setLastRoundFlag();
        if (lastRound && isPlaying.hasFirstPlayerSeat()){
            isPlaying = nextPlayer;
            setLastTurnFlag();
        }
        if (lastTurn) { //game end
            for (Player p: leaderBoard) {
                p.updateFinalScore();
            }
            getLeaderBoard();
        }
    };


    /**
     * adds a new player to the lobby - fristPlayerSeat set to false by default
     * @param nick
     */
    public void addPlayer(String nick){
        Player player = new Player(nick, choosePersonalGoals(), false, board);
        playersList.add(player);
        leaderBoard.add(player);
    }

    /**
     * removes player with nickname nick
     * @param nick
     */
    public void removePlayer(String nick){
        for (Player p: playersList)
            if (p.getNickname() == nick) playersList.remove(p);
    }

    /** comparator is used to keep the leaderboard ordered by score */
    private class scoreComparator implements Comparator<Player>{
        public int compare(Player p1, Player p2){
            return p2.score - p1.score;
        }
    }


    //***    setters     ***//
    /** picks random player to start the game */
    private void setFirstPlayer(){
        Random random = new Random();
        int starter = random.nextInt(playersList.size() - 1);
        playersList.get(starter).setFirstPlayerSeat();
        iterator = playersList.iterator();
        isPlaying = playersList.get(starter);
    }

    //stub
    private void chooseCommonGoals(){
        commonGoalCards.add(new CommonGoalCard(new Diagonal(), playersList.size()));
        commonGoalCards.add(new CommonGoalCard(new EightofSameType(), playersList.size()));
    }
    //stub
    private PersonalGoalCard choosePersonalGoals(){
        return new PersonalGoalCard();
    }

    private void setBoard(){
        board = new Board(playersList.size());
        for (Player p: playersList) p.setBoard(board);
    }

    private void setIsPlaying(Player player){
        this.isPlaying = player;
    }

    private void setLastTurnFlag(){
        this.lastTurn = true;
    }
    private void setLastRoundFlag(){
        this.lastRound = true;
    }
    private boolean isLastTurn(){
        return lastTurn;
    }
    private boolean isLastRound(){
        return lastRound;
    }

    //***   getters   ***//
    //prints leaderboard to output
    public void getLeaderBoard(){
        int i = 0;
        for (Player p: leaderBoard) {
            System.out.println(i + ". " + p.getNickname() + ", score: " + p.score);
            i++;
        }
    }

    public Board getBoard(){
        return this.board;
    }
}
