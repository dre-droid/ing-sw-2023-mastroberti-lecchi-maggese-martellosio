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
    private int turnCount;
    private boolean lastTurn;
    private boolean lastRound;
    private boolean gameHasEnded;
    private boolean gameHasStarted;


    /**
     * Constructor - creates a new instance of a game
     * @param numOfPlayers - first player to connect and consequently create the game sets the number of players for such game
     */
    public Game(int numOfPlayers){
        this.numOfPlayers = numOfPlayers;
        playersList = new ArrayList<>();
        leaderBoard = new ArrayList<>();
        commonGoalCards = new ArrayList<>();
        turnCount = 1;
        lastTurn = false;
        lastRound = false;
        gameHasEnded=false;
        gameHasStarted=false;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    /**
     * This method is used to check if this the game is in its last turn
     * @return true if it's the last turn, false otherwise
     */
    public boolean isLastTurn(){
        return lastTurn;
    }

    /**
     * This method is used to check if the game is in its last round
     * @return true if it's the last round, false otherwise
     */
    public boolean isLastRound(){
        return lastRound;
    }

    /**
     *this method is used to check if the game has ended
     * @return true if the game is over, false otherwise
     */
    public boolean hasTheGameEnded(){
        return gameHasEnded;
    }



    /**
     * @author Andrea Mastroberti
     * after players have been added to the lobby,
     * game starts: Sets first player, Assigns personal goal cards, Fills the board and chooses the common goal cards
     */
    public void gameStartSetup() throws Exception{
       if (numOfPlayers != playersList.size()) throw new Exception("Not enough players have connected yet!");
       setBoard();
       setFirstPlayer();
       chooseCommonGoals();
       drawPersonalGoalCard();
       this.gameHasStarted = true;
       System.out.println("//gameStartSetup// Players list " + playersList);
       System.out.println();

    }

    public List<Tile> drawsFromBoard(int x,int y,int amount, Board.Direction direction,Player player) throws InvalidMoveException{
        if(!gameHasEnded){
            if(player.getNickname().equals(isPlaying.getNickname())) {
                throw new InvalidMoveException(player.getNickname() + " it's not your turn!!!!");
            }
            List<Tile> tiles = player.drawTiles(x, y, amount, direction);
            return tiles;
        }
        return null;
    }

    public boolean insertTilesInShelf(List<Tile> drawnTiles,int column,Player player) throws InvalidMoveException {
        if(!gameHasEnded){
            if(!player.getNickname().equals(isPlaying.getNickname()))
                throw new InvalidMoveException(player.getNickname()+" it's not your turn!!!");
            if(player.insertTiles(drawnTiles,column))
                return true;
        }
        return false;
    }

    public boolean checkIfCommonGoalN1IsFulfilled(Player player){
        if(!gameHasEnded){
            if(player.getNickname().equals(isPlaying.getNickname())){
                if (commonGoalCards.get(0).isSatisfiedBy(isPlaying)) {
                    try {
                        isPlaying.addScoringToken(commonGoalCards.get(0).getReward(isPlaying));
                        return true;
                    }
                    catch (CannotCollectRewardException e) {
                        return false;
                    }
                }
            }
            return false;
        }
        return false;

    }

    public boolean checkIfCommonGoalN2IsFulfilled(Player player){
        if(!gameHasEnded){
            if(player.getNickname().equals(isPlaying.getNickname())){
                if (commonGoalCards.get(1).isSatisfiedBy(isPlaying)) {
                    try {
                        isPlaying.addScoringToken(commonGoalCards.get(1).getReward(isPlaying));
                        return true;
                    }
                    catch (CannotCollectRewardException e) {
                        return false;
                    }
                }
            }
            return false;
        }
        return false;

    }
    /*
    public boolean checkIfCommonGoalsAreFulfilled(Player player){
        if(player.getNickname().equals(isPlaying.getNickname())){
            for (CommonGoalCard c: commonGoalCards)
                if (c.isSatisfiedBy(isPlaying)) {
                    try {
                        isPlaying.addScoringToken(c.getReward(isPlaying));
                        return true;
                    }
                    catch (CannotCollectRewardException e) {
                        e.printStackTrace();
                    }
                }
            return false;
        }
        return false;
    }*/

    public void endOfTurn(Player player){
        if(!gameHasEnded){
            if(player.getNickname().equals(isPlaying.getNickname())){
                //if someone's shelf is full then enter the last round
                if (isPlaying.hasEndGameToken()) setLastRoundFlag();
                //check if someone has fulfilled a commongoal


                //refill board if necessary
                board.refill();

                //update score and leaderboard
                isPlaying.updateScore();
                Collections.sort(leaderBoard, new scoreComparator());

                //next turn and end game logic
                Player nextPlayer;
                if (!iterator.hasNext()) iterator = playersList.iterator(); //if reached end of list, go to beginning
                nextPlayer = iterator.next();

                if (lastRound && isPlaying.hasFirstPlayerSeat()){       //last turn
                    isPlaying = nextPlayer;
                    setLastTurnFlag();
                }
                if (lastTurn) { //game end                              //game end
                    for (Player p: leaderBoard) {
                        p.updateFinalScore();
                    }
                    printLeaderBoard();
                    endGame();
                }
                else isPlaying = nextPlayer;

            }
        }
    }

    /**
     * @author Andrea Mastroberti
     * makes the player draw from the board and inserts tile in the shelf, then changes the isPlaying Player
     * parameters to call drawTiles and insertTiles methods in class Player
     * @param x rows of game board [0 ... 9]
     * @param y columns of game board [0 ... 9]
     * @param amount amount of tiles to be drawn [0 ... 3]
     * @param direction draw direction [RIGHT, LEFT, UP, DOWN]
     * @param column shelf column to place drawn tiles [0 ... 5]
     */
    public void playTurn(int x, int y, int amount, Board.Direction direction, int column) throws InvalidMoveException{
        System.out.println("********* Turn n." + turnCount + " - " + isPlaying.getNickname() + " is playing." + "*********");

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

        if (isPlaying.hasEndGameToken()) setLastRoundFlag();    //last round
        if (lastRound && isPlaying.hasFirstPlayerSeat()){       //last turn
            isPlaying = nextPlayer;
            setLastTurnFlag();
        }
        if (lastTurn) { //game end                              //game end
            for (Player p: leaderBoard) {
                p.updateFinalScore();
            }
            printLeaderBoard();
        }
        else isPlaying = nextPlayer;

        turnCount++;                                            //increase turnCount

        getBoard().printGridMap();
        System.out.println();
        System.out.println("Leaderboard");
        printLeaderBoard();
        System.out.println("******************************\n");
    }

    /**
     * @return true if playersList.size() has reache numOfPlayers
     */
    public boolean hasGameStarted(){
        return gameHasStarted;
    }

    /**
     * If playersList isn't full, adds a new player to the leaderBoard and to playersList - fristPlayerSeat set to false by default.
     * When all players have joined, starts the game.
     *
     * @param nick - nickname
     */
   public boolean addPlayer(String nick){
       if(!hasGameStarted()) {
           if(playersList.stream().map(Player::getNickname).noneMatch(n->n.equals(nick))){
               Player player = new Player(nick, false, board);
               playersList.add(player);
               leaderBoard.add(player);
               if (playersList.size() == numOfPlayers)
                   try {
                       this.gameStartSetup();
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               return true;
           }
       }
       return false;
    }

    /**
     * removes player with nickname nick
     * @param nick - nickname
     */
    public void removePlayer(String nick){
        for (Player p: playersList)
            if (p.getNickname().equals(nick)) playersList.remove(p);
    }

    /**
     * comparator used to keep the leaderboard in descending ordered by score
     */
    private class scoreComparator implements Comparator<Player>{
        public int compare(Player p1, Player p2){
            return p2.getScore() - p1.getScore();
        }
    }


    //***    setters     ***//
    /**
     * Picks random player to start the game. Sets the iterator to point to such player in the list. Sets isPlaying to the selected player.
     */
    private void setFirstPlayer(){
        //get random value, set firstPlayerSeat
        Random random = new Random();
        int starter = random.nextInt(playersList.size() - 1);
        playersList.get(starter).setFirstPlayerSeat();
        System.out.println("//setFirstPlayer// Starting player: " + playersList.get(starter));

        //set iterator, isPlaying
        iterator = playersList.iterator();
        while (!iterator.next().getNickname().equals(playersList.get(starter).getNickname()));
        isPlaying = playersList.get(starter);
    }

    /**
     * Chooses two random and distinct common goal cards and adds them to the commonGoalCards list
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

    /**
     * Creates new board as a function of the number of players - also gives players a reference to the Board instance variable
     */
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
    public void endGame(){
        this.gameHasEnded=true;
    }


    //***   getters   ***//
    public Board getBoard(){
        return this.board;
    }
    public Player getIsPlaying(){
        return isPlaying;
    }
    public List<Player> getPlayerList(){
        return playersList;
    }

    /**
     * Prints leaderboard to console
     */
    public void printLeaderBoard(){
        int i = 0;
        for (Player p: leaderBoard) {
            System.out.println(i + 1 + ". " + p.getNickname() + ", score: " + p.getScore());
            i++;
        }
        System.out.println();
    }
    public List<Player> getLeaderBoard(){
        return this.leaderBoard;
    }
    public List<CommonGoalCard> getCommonGoalCards(){return commonGoalCards;}

    /**
     * @author DiegoLecchi
     * assigns a personal goal card randomly to each player in playerList
     */
    private void drawPersonalGoalCard() {
        int[] numberAlreadyDrawn = new int[playersList.size()];
        Random rand = new Random();
        int randomNum = 0;
        int i=0;
        for (Player p: playersList) {
            while(checkArrayForDuplicate(numberAlreadyDrawn, randomNum)){
                randomNum = rand.nextInt((12 - 1) + 1) + 1;
            }
            numberAlreadyDrawn[i]=randomNum;
            i++;
            switch (randomNum) {
                case 1 -> p.setPersonalGoalCard(new PersonalGoalCard1());
                case 2 -> p.setPersonalGoalCard(new PersonalGoalCard2());
                case 3 -> p.setPersonalGoalCard(new PersonalGoalCard3());
                case 4 -> p.setPersonalGoalCard(new PersonalGoalCard4());
                case 5 -> p.setPersonalGoalCard(new PersonalGoalCard5());
                case 6 -> p.setPersonalGoalCard(new PersonalGoalCard6());
                case 7 -> p.setPersonalGoalCard(new PersonalGoalCard7());
                case 8 -> p.setPersonalGoalCard(new PersonalGoalCard8());
                case 9 -> p.setPersonalGoalCard(new PersonalGoalCard9());
                case 10 -> p.setPersonalGoalCard(new PersonalGoalCard10());
                case 11 -> p.setPersonalGoalCard(new PersonalGoalCard11());
                case 12 -> p.setPersonalGoalCard(new PersonalGoalCard12());
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

}

