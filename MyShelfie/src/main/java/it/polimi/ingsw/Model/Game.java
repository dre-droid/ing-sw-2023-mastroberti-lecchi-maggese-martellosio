package main.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.*;
import main.java.it.polimi.ingsw.Model.PersonalGoalCards.*;

import java.util.List;
import java.util.Random;

import java.util.*;

public class Game {
    public Player isPlaying;//should be private
    private final ArrayList<Player> playersList;
    private int numOfPlayers;
    private final List<Player> leaderBoard;
    private Iterator<Player> iterator;
    private final List<CommonGoalCard> commonGoalCards;
    private Board board;
    private boolean lastTurn;
    private boolean lastRound;


    /**
     * Constructor - creates a new instance of a game
     * @param numOfPlayers - first player to connect and consequently create
     *                       the game sets the number of players for such game
     */
    public Game(int numOfPlayers){
        this.numOfPlayers = numOfPlayers;
        playersList = new ArrayList<>();
        leaderBoard = new ArrayList<>();
        commonGoalCards = new ArrayList<>();
        lastTurn = false;
        lastRound = false;
    }

    /**
     * after players have been added to the lobby,
     * game starts: Sets first player, Assigns personal goal cards, Fills the board and chooses the common goal cards
     */
    public void gameStartSetup() throws Exception{
       if (!hasGameStarted()) throw new Exception("Not enough players have connected yet!");
       setFirstPlayer();
       setBoard();
       chooseCommonGoals();
       drawPersonalGoalCard();
       System.out.println("Players list " + playersList);

    }

    /**
     * makes the player draw from the board and inserts tile in the shelf, then changes the isPlaying Player
     * parameters to call drawTiles and insertTiles methods in class Player
     */
    public void playTurn(int x, int y, int amount, Board.Direction direction, int column){
        //player draws from board and inserts in his shelf - is the shelf is full sets lastTurnFlag
        List<Tile> tiles = isPlaying.drawTiles(x, y, amount, direction);
        isPlaying.insertTiles(tiles, column);
        isPlaying.printShelf();
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
        Player nextPlayer;
        if (!iterator.hasNext()) iterator = playersList.iterator(); //if reached end of list, go to beginning
        nextPlayer = iterator.next();
        System.out.println("NEXT PLAYER: " + nextPlayer);

        if (isPlaying.hasEndGameToken()) setLastRoundFlag();    //last round
        if (lastRound && isPlaying.hasFirstPlayerSeat()){       //last turn
            isPlaying = nextPlayer;
            setLastTurnFlag();
        }
        if (lastTurn) { //game end                              //game end
            for (Player p: leaderBoard) {
                p.updateFinalScore();
            }
            getLeaderBoard();
        }
        else isPlaying = nextPlayer;
    }

    public boolean hasGameStarted(){
        return numOfPlayers == playersList.size();
    }

    /**
     * adds a new player to the lobby - fristPlayerSeat set to false by default
     * @param nick - nickname
     */
   public void addPlayer(String nick){
       if(!hasGameStarted()){
           Player player = new Player(nick, false, board);
           playersList.add(player);
           leaderBoard.add(player);
           if(playersList.size()==numOfPlayers)
               try{
                   this.gameStartSetup();
               }catch(Exception e){
                   e.printStackTrace();
               }
       }


    }

    /**
     * removes player with nickname nick
     * @param nick - nickname
     */
    public void removePlayer(String nick){
        for (Player p: playersList)
            if (p.getNickname().equals(nick)) playersList.remove(p);
    }

    /** comparator is used to keep the leaderboard ordered by score */
    private class scoreComparator implements Comparator<Player>{
        public int compare(Player p1, Player p2){
            return p2.getScore() - p1.getScore();
        }
    }


    //***    setters     ***//
    /**
     * picks random player to start the game
     * sets the iterator to point to the selected player in the list
     * sets isPlaying to the selected player
     */
    private void setFirstPlayer(){
        //get random value, set firstPlayerSeat
        Random random = new Random();
        int starter = random.nextInt(playersList.size() - 1);
        playersList.get(starter).setFirstPlayerSeat();
        System.out.println("Starting player: " + playersList.get(starter));

        //set iterator, isPlaying
        iterator = playersList.iterator();
        while (!iterator.next().getNickname().equals(playersList.get(starter).getNickname()));
        isPlaying = playersList.get(starter);
    }

    /**
     * chooses two distinct and random common goal cards and adds them to the commonGoalCards list
     */
    public void chooseCommonGoals(){
        int randNum1, randNum2;
        randNum1 = new Random().nextInt(12);
        randNum2 = new Random().nextInt(12);
        while (randNum2 == randNum1) randNum2 = new Random().nextInt(12 - 1);   //makes sure the CGcards are distinct
        switch (randNum1) {
            case 0 -> commonGoalCards.add(new CommonGoalCard(new Diagonal(), this.numOfPlayers));
            case 1 -> commonGoalCards.add(new CommonGoalCard(new EightofSameType(), this.numOfPlayers));
            case 2 -> commonGoalCards.add(new CommonGoalCard(new FourCornerOfTheSameType(), this.numOfPlayers));
            case 3 -> commonGoalCards.add(new CommonGoalCard(new FourGroupsOfAtLeastFourSameTypeTiles(), this.numOfPlayers));
            case 4 -> commonGoalCards.add(new CommonGoalCard(new FourRowsOfMaxThreeDifferentTypes(), this.numOfPlayers));
            case 5 -> commonGoalCards.add(new CommonGoalCard(new IncreasingOrDecreasingHeight(), this.numOfPlayers));
            case 6 -> commonGoalCards.add(new CommonGoalCard(new SixGroupsOfAtLeastTwoSameTypeTiles(), this.numOfPlayers));
            case 7 -> commonGoalCards.add(new CommonGoalCard(new SquaredShapedGroups(), this.numOfPlayers));
            case 8 -> commonGoalCards.add(new CommonGoalCard(new ThreeColumnsOfMaxThreeDifferentTypes(), this.numOfPlayers));
            case 9 -> commonGoalCards.add(new CommonGoalCard(new TwoColumnsOfDifferentTypes(), this.numOfPlayers));
            case 10 -> commonGoalCards.add(new CommonGoalCard(new TwoLinesOfDifferentTypes(), this.numOfPlayers));
            case 11 -> commonGoalCards.add(new CommonGoalCard(new XShapedTiles(), this.numOfPlayers));
        }
        switch(randNum2){
            case 0 -> commonGoalCards.add(new CommonGoalCard(new Diagonal(), this.numOfPlayers));
            case 1 -> commonGoalCards.add(new CommonGoalCard(new EightofSameType(), this.numOfPlayers));
            case 2 -> commonGoalCards.add(new CommonGoalCard(new FourCornerOfTheSameType(), this.numOfPlayers));
            case 3 -> commonGoalCards.add(new CommonGoalCard(new FourGroupsOfAtLeastFourSameTypeTiles(), this.numOfPlayers));
            case 4 -> commonGoalCards.add(new CommonGoalCard(new FourRowsOfMaxThreeDifferentTypes(), this.numOfPlayers));
            case 5 -> commonGoalCards.add(new CommonGoalCard(new IncreasingOrDecreasingHeight(), this.numOfPlayers));
            case 6 -> commonGoalCards.add(new CommonGoalCard(new SixGroupsOfAtLeastTwoSameTypeTiles(), this.numOfPlayers));
            case 7 -> commonGoalCards.add(new CommonGoalCard(new SquaredShapedGroups(), this.numOfPlayers));
            case 8 -> commonGoalCards.add(new CommonGoalCard(new ThreeColumnsOfMaxThreeDifferentTypes(), this.numOfPlayers));
            case 9 -> commonGoalCards.add(new CommonGoalCard(new TwoColumnsOfDifferentTypes(), this.numOfPlayers));
            case 10 -> commonGoalCards.add(new CommonGoalCard(new TwoLinesOfDifferentTypes(), this.numOfPlayers));
            case 11 -> commonGoalCards.add(new CommonGoalCard(new XShapedTiles(), this.numOfPlayers));
        }
    }

    private void setBoard(){
        board = new Board(playersList.size());
        for (Player p: playersList) p.setBoard(board); //sets players reference to board
    }

    private void setLastTurnFlag(){
        this.lastTurn = true;
    }
    private void setLastRoundFlag(){
        this.lastRound = true;
    }


    //***   getters   ***//
    //prints leaderboard to output
    public void getLeaderBoard(){
        int i = 0;
        for (Player p: leaderBoard) {
            System.out.println(i + 1 + ". " + p.getNickname() + ", score: " + p.getScore());
            i++;
        }
        System.out.println();
    }

    public Board getBoard(){
        return this.board;
    }

    /**
     * @author DiegoLecchi
     * assigns a personal goal card randomly to each player in playerList
     */
    private void drawPersonalGoalCard() {
        int[] numberAlreadyDrawn = new int[playersList.size()];
        Random rand = new Random();
        int randomNum = 0;
        for (Player p: playersList) {
            while(checkArrayForDuplicate(numberAlreadyDrawn, randomNum)){
                randomNum = rand.nextInt((12 - 1) + 1) + 1;
            }
            switch(randomNum) {
                case 1:
                    p.setPersonalGoalCard(new PersonalGoalCard1());
                    break;
                case 2:
                    p.setPersonalGoalCard(new PersonalGoalCard2());
                    break;
                case 3:
                    p.setPersonalGoalCard(new PersonalGoalCard3());
                    break;
                case 4:
                    p.setPersonalGoalCard(new PersonalGoalCard4());
                    break;
                case 5:
                    p.setPersonalGoalCard(new PersonalGoalCard5());
                    break;
                case 6:
                    p.setPersonalGoalCard(new PersonalGoalCard6());
                    break;
                case 7:
                    p.setPersonalGoalCard(new PersonalGoalCard7());
                    break;
                case 8:
                    p.setPersonalGoalCard(new PersonalGoalCard8());
                    break;
                case 9:
                    p.setPersonalGoalCard(new PersonalGoalCard9());
                    break;
                case 10:
                    p.setPersonalGoalCard(new PersonalGoalCard10());
                    break;
                case 11:
                    p.setPersonalGoalCard(new PersonalGoalCard11());
                    break;
                case 12:
                    p.setPersonalGoalCard(new PersonalGoalCard12());
                    break;
            }
        }
    }

    private boolean checkArrayForDuplicate(int[] numberAlreadyDrawn, int randomNum){
        for (int i = 0; i <playersList.size(); i++) {
            if(randomNum == numberAlreadyDrawn[i])
                return true;
        }
        return false;
    }

    public List<Player> getPlayerList(){
        return playersList;
    }
    public List<CommonGoalCard> getCommonGoalCards(){return commonGoalCards;}
}

