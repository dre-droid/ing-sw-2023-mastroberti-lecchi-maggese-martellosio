package main.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.EightofSameType;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.StrategyCommonGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    Player isPlaying;
    List<Player> playersList;
    List<CommonGoalCard> commonGoalCards;
    Board board;
    boolean lastTurn;

    /**
     * Creates a new instance of a game
     */
    public Game(){
        playersList = new ArrayList<>();
    }


    /**
     * adds a new player to the lobby - fristPlayerSeat set to false by default
     * @param nick
     */
    public void addPlayer(String nick){
        Player player = new Player(nick, choosePersonalGoals(), false, board);
        playersList.add(player);
    }

    public void playTurn(int x, int y, int amount, Board.Direction direction, int column){
        isPlaying.insertTiles(isPlaying.drawTiles(x, y, amount, direction), column);

    };

    //stub
    private CommonGoalCard chooseCommonGoals(){
        return new CommonGoalCard(new EightofSameType(), 2);
    }
    //stub
    private PersonalGoalCard choosePersonalGoals(){
        return new PersonalGoalCard();
    }

    public void setBoard(){
        board = new Board(playersList.size());
    }

    public void setFirstPlayer(){
        Random random = new Random();
        int starter = random.nextInt(playersList.size() - 1);
        playersList.get(starter);
    }


    //boolean getNextPlayer(){}
    //void removePlayer(){}
    //boolean isLastTurn(){}
    //void setLastTurnFlag(){}
    //List<Player> getLeaderBoard(){}

}
