package main.java.it.polimi.ingsw.Model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;
import java.util.*;

public class Game {
    private Player isPlaying;
    private ArrayList<Player> playersList;
    private int numOfPlayers;
    private ArrayList<Player> leaderBoard;
    private Iterator<Player> iterator;
    private List<CommonGoalCard> commonGoalCards;
    private final HashMap<Integer, PersonalGoalCard> validTilesMap = new HashMap<>();
    private Board board;
    private int turnCount;
    int lastPlayerIndex;
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

    /**
     * This constructor is used to load the game progress from a json file
     * @param savedProgressFilePath the file path of the json file where the game progress has been saved
     */
    public Game(String savedProgressFilePath){
        playersList = new ArrayList<>();
        leaderBoard = new ArrayList<>();
        commonGoalCards = new ArrayList<>();
        loadGameProgress(savedProgressFilePath);
    }

    /**
     * Used for testing: calls gameStartSetup methods withing the constructor for ease of testing.
     */
    public Game(int numOfPlayers, Board board, ArrayList<Player> playerList){
        this.numOfPlayers = numOfPlayers;
        this.board = board;
        this.playersList = playerList;
        leaderBoard = new ArrayList<>();
        leaderBoard.add(playerList.get(0));
        leaderBoard.add(playerList.get(1));
        commonGoalCards = new ArrayList<>();
        turnCount = 1;
        lastTurn = false;
        lastRound = false;
        gameHasEnded=false;
        gameHasStarted=false;
        fillValidTileMap();

        //setfirstplayer  //second player to start
        playersList.get(1).setFirstPlayerSeat();
        iterator = playersList.iterator();
        iterator.next();
        iterator.next();
        isPlaying = playersList.get(1);

        //chooseCommonGoals
        commonGoalCards.add(new CommonGoalCard(new Diagonal(), this.numOfPlayers));
        commonGoalCards.add(new CommonGoalCard(new FourCornerOfTheSameType(), this.numOfPlayers));

        //drawPersonalGoalCards
        playerList.get(0).setPersonalGoalCard(validTilesMap.get(1));
        playerList.get(1).setPersonalGoalCard(validTilesMap.get(2));

        this.gameHasStarted = true;
    }

    /**
     * @author Andrea Mastroberti
     * after players have been added to the lobby,
     * game starts: Sets first player, Assigns personal goal cards, Fills the board and chooses the common goal cards
     */
    public void gameStartSetup() throws Exception{
       if (numOfPlayers != playersList.size()) throw new Exception("Not enough players have connected yet!");
       setBoard();
       fillValidTileMap();
       setFirstPlayer();
       chooseCommonGoals();
       drawPersonalGoalCard();
       this.gameHasStarted = true;
       System.out.println("Game is starting... Players list: " + playersList);
       System.out.println();

    }

    /**
     * If playersList isn't full, adds a new player to the leaderBoard and to playersList - fristPlayerSeat set to false by default.
     * When all players have joined, starts the game.
     *
     * @param nick - nickname
     */
    public boolean addPlayer(String nick){
        if(!hasGameStarted()) {
            if(playersList.stream().map(Player::getNickname).noneMatch(n->n.equals(nick)) && !nick.isBlank()){
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
     * This method is used to draw tiles from board
     * @param x : represents the row of the first tile you want to draw from
     * @param y : represents the column of the first tile you want to draw from
     * @param amount : the number of tiles you want to draw
     * @param direction : starting from the first tiles position just specified, the direction to follow to pick the remaining ones
     * @param playerNickname : player that is performing the drawing
     * @return : list ot tiles drawn from Board if the tiles can be drawn, null otherwise
     * @throws InvalidMoveException
     */
    public List<Tile> drawsFromBoard(int x,int y,int amount, Board.Direction direction,String playerNickname) throws InvalidMoveException{
        if(!gameHasEnded){
            System.out.println("("+x+","+y+") amount = "+amount+" direction ="+direction.toString());
            if(!playerNickname.equals(isPlaying.getNickname())) {
                throw new InvalidMoveException(playerNickname + " it's not your turn!!!!");
            }
            List<Tile> tiles = board.drawTiles(x, y, amount, direction);
            return tiles;
        }
        return null;
    }

    /**
     * This method is used to insert the tiles you just have drawn into player's shelf
     * @param drawnTiles : the list of tiles resulting from drawsFromBoard
     * @param column : the column in player's shelf you want to insert drawn tiles
     * @param player : the player who's playing the turn, performing insertion
     * @return: true if insertion is correctly executed, false otherwise
     * @throws InvalidMoveException
     */
    public boolean insertTilesInShelf(List<Tile> drawnTiles,int column,Player player) throws InvalidMoveException {
        if(!gameHasEnded){
            System.out.println("insert shelf ok called");
            if(!player.getNickname().equals(isPlaying.getNickname()))
                throw new InvalidMoveException(player.getNickname()+" it's not your turn!!!");
            if(player.insertTiles(drawnTiles,column))
                return true;
            else{
                System.out.println("problemone nell'inserimento");
            }
        }
        return false;
    }

    /**
     * This method is used to handle player's end turn
     * @param player: the player that is playing and is about to end his turn
     */
    public void endOfTurn(Player player){
        if(!gameHasEnded){
            if(player.getNickname().equals(isPlaying.getNickname())){
                //refill board if necessary
                board.refill();

                //update score and leaderboard
                isPlaying.updateScore();
                Collections.sort(leaderBoard, new scoreComparator());

                //select next player
                Player nextPlayer;
                if (!iterator.hasNext()) iterator = playersList.iterator(); //if reached end of list, go to beginning
                nextPlayer = iterator.next();

                // if last turn
                if ((lastRound || isPlaying.hasEndGameToken()) && isPlaying.getNickname().equals(playersList.get(lastPlayerIndex).getNickname())){
                    playersList.forEach(Player::updateFinalScore);
                    endGame();
                }
                else{
                    if (isPlaying.hasEndGameToken())
                        setLastRoundFlag(); // if someone's shelf is full then enter the last round
                    isPlaying = nextPlayer;
                }

                turnCount++;                //increase turnCount
            }
        }
    }

    //******************
    //*** used by Socket ***
    /**
     *  This method  makes the player draw from the board and inserts tile in the shelf, then changes the isPlaying Player
     * parameters to call drawTiles and insertTiles methods in class Player
     * @param x rows of game board [0 ... 9]
     * @param y columns of game board [0 ... 9]
     * @param amount amount of tiles to be drawn [0 ... 3]
     * @param direction draw direction [RIGHT, LEFT, UP, DOWN]
     * @param column shelf column to place drawn tiles [0 ... 5]
     * @author Andrea Mastroberti
     */
    public void playTurn(int x, int y, int amount, Board.Direction direction, int column, List<Tile> reorderedTiles) throws InvalidMoveException{
        System.out.println("********* Turn n." + turnCount + " - " + isPlaying.getNickname() + " is playing." + "*********");
        //player draws from board and inserts in his shelf - is the shelf is full sets lastTurnFlag
        List<Tile> drawnTiles = board.drawTiles(x, y, amount, direction);

        //check that drawn tiles match parameter reorderedTiles, else throw exception
        boolean missingTile = true;
        for (Tile t: reorderedTiles) {
            for (Tile j : drawnTiles) {
                if (t.toString().equals(j.toString())) missingTile = false;
            }
            if (missingTile) throw new InvalidMoveException("Drawn tiles don't match reordered tiles!");
        }

        //fix insert tiles ordering
        Player current = getPlayerList().stream().filter(player -> player.getNickname().equals(isPlaying.getNickname())).toList().get(0);
        insertTilesInShelf(reorderedTiles,column,current);


        //isPlaying.insertTiles(reorderedTiles, column);

        //check common goal and eventually give player token
        for (CommonGoalCard c: commonGoalCards)
            if (c.isSatisfiedBy(isPlaying)) {
                try {
                    isPlaying.addScoringToken(c.getReward(isPlaying));
                }
                catch (CannotCollectRewardException e) {

                }
            }

       endOfTurn(isPlaying);
    }
    /**
     * Removes player with nickname nick from playersList and leaderBoard when client disconnects before game has started.
     * Should only be called if game hasn't started.
     */
    public void removePlayerOld(String nick) {
       for (Player p: playersList) if (p.getNickname().equals(nick)){
           playersList.remove(p);
           leaderBoard.remove(p);
       }
    }
    /**
     * Removes player with nickname nick from playersList and leaderBoard when client disconnects before game has started.
     * Should only be called if game hasn't started.
     */
    public void removePlayer(String nick) {
        Iterator<Player> iterator = playersList.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.getNickname().equals(nick)) {
                iterator.remove(); // Use iterator.remove() to safely remove the element
                leaderBoard.remove(player);
                break; // Exit the loop after removing the player
            }
        }
    }
    /**
     * comparator used to keep the leaderboard in descending ordered by score (and by distance from the
     * first player if the score is tied)
     */
    private class scoreComparator implements Comparator<Player>{
        public int compare(Player p1, Player p2){
            if(p1.getScore() == p2.getScore()){
                return distanceFromFirstPlayer(p2)-distanceFromFirstPlayer(p1);
            }
            return p2.getScore() - p1.getScore();
        }
    }

    /**
     * this method is used to get the distance of a player from the player who holds the first player seat
     * @param player : player you want to check the distance from first player
     * @return the distance of the player from the first player
     */
    private int distanceFromFirstPlayer(Player player){
        int indexOfPlayer = playersList.indexOf(player);
        int indexOfFirstPlayer=-1;
        for(Player p: playersList){
            if(p.hasFirstPlayerSeat()){
                indexOfFirstPlayer = playersList.indexOf(p);
            }
        }
        if(indexOfPlayer<indexOfFirstPlayer){
            return indexOfFirstPlayer-indexOfPlayer;
        }
        else{
            return playersList.size()-indexOfPlayer+indexOfFirstPlayer;
        }
    }
    //*********************

    //***    setters     ***//
    /**
     * Picks random player to start the game. Sets the iterator to point to such player in the list. Sets isPlaying to the selected player.
     */
    private void setFirstPlayer(){
        //get random value, set firstPlayerSeat
        Random random = new Random();
        int starter = random.nextInt(playersList.size());
        playersList.get(starter).setFirstPlayerSeat();

        //set iterator, isPlaying
        iterator = playersList.iterator();
        while (!iterator.next().getNickname().equals(playersList.get(starter).getNickname()));
        isPlaying = playersList.get(starter);
        lastPlayerIndex = playersList.indexOf(isPlaying) == 0 ? playersList.size() - 1 : playersList.indexOf(isPlaying) - 1;
        System.out.println("Last player index: "+ lastPlayerIndex + ". Player: " + playersList.get(lastPlayerIndex));
        playersList.stream().forEach(System.out::println);
        System.out.println("Last player index is " + lastPlayerIndex);
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
    public HashMap<Integer, PersonalGoalCard> getValidTilesMap() {return validTilesMap;}
    public int getNumOfPlayers() {
        return numOfPlayers;
    }
    public List<Player> getLeaderBoard(){
        return this.leaderBoard;
    }
    public List<CommonGoalCard> getCommonGoalCards(){return commonGoalCards;}

    //*** boolean methods ***//
    public boolean hasGameStarted() {
        return playersList.size() == numOfPlayers;
    }
    /**
     * Used by drawPersonalGoalCard to check if a number has already been drawn
     * @param numberAlreadyDrawn vector of already drawn numbers
     * @param randomNum random generated number
     * @return true if randomNum equals to another number in vector numberAlreadyDrawn, false otherwise
     */
    private boolean checkArrayForDuplicate(int[] numberAlreadyDrawn, int randomNum){
        for (int i = 0; i <playersList.size(); i++) {
            if(randomNum == numberAlreadyDrawn[i])
                return true;
        }
        return false;
    }
    /**
     *This method is used to check if the game has ended
     * @return true if the game is over, false otherwise
     */
    public boolean hasTheGameEnded(){
        return gameHasEnded;
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
                randomNum = rand.nextInt((validTilesMap.size() - 1) + 1) + 1;
            }
            numberAlreadyDrawn[i]=randomNum;
            i++;
            //pg.initializeValidTiles(randomNum);

            p.setPersonalGoalCard(validTilesMap.get(randomNum));
        }
    }

    /**
     * @author DiegoLecchi
     * fills map validTilesMap with every personalGoalCard. Extendable with new personal goal cards in the future
     */
    public void fillValidTileMap(){
        PersonalGoalCard personalGoalCard1 = new PersonalGoalCard();
        personalGoalCard1.validTiles.getGrid()[0][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard1.validTiles.getGrid()[0][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        personalGoalCard1.validTiles.getGrid()[1][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard1.validTiles.getGrid()[2][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard1.validTiles.getGrid()[3][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard1.validTiles.getGrid()[5][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        validTilesMap.put(1, personalGoalCard1);

        PersonalGoalCard personalGoalCard2 = new PersonalGoalCard();
        personalGoalCard2.validTiles.getGrid()[1][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard2.validTiles.getGrid()[2][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard2.validTiles.getGrid()[2][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard2.validTiles.getGrid()[3][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard2.validTiles.getGrid()[4][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard2.validTiles.getGrid()[5][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(2, personalGoalCard2);

        PersonalGoalCard personalGoalCard3 = new PersonalGoalCard();
        personalGoalCard3.validTiles.getGrid()[2][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard3.validTiles.getGrid()[3][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard3.validTiles.getGrid()[1][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard3.validTiles.getGrid()[5][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard3.validTiles.getGrid()[3][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard3.validTiles.getGrid()[1][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(3, personalGoalCard3);

        PersonalGoalCard personalGoalCard4 = new PersonalGoalCard();
        personalGoalCard4.validTiles.getGrid()[3][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard4.validTiles.getGrid()[4][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard4.validTiles.getGrid()[0][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard4.validTiles.getGrid()[4][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard4.validTiles.getGrid()[2][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard4.validTiles.getGrid()[2][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(4, personalGoalCard4);

        PersonalGoalCard personalGoalCard5 = new PersonalGoalCard();
        personalGoalCard5.validTiles.getGrid()[4][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard5.validTiles.getGrid()[5][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard5.validTiles.getGrid()[5][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard5.validTiles.getGrid()[3][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard5.validTiles.getGrid()[1][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard5.validTiles.getGrid()[3][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(5, personalGoalCard5);

        PersonalGoalCard personalGoalCard6 = new PersonalGoalCard();
        personalGoalCard6.validTiles.getGrid()[5][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard6.validTiles.getGrid()[0][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard6.validTiles.getGrid()[4][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard6.validTiles.getGrid()[2][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard6.validTiles.getGrid()[0][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard6.validTiles.getGrid()[4][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(6, personalGoalCard6);

        PersonalGoalCard personalGoalCard7 = new PersonalGoalCard();
        personalGoalCard7.validTiles.getGrid()[2][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard7.validTiles.getGrid()[0][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard7.validTiles.getGrid()[4][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard7.validTiles.getGrid()[5][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard7.validTiles.getGrid()[3][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard7.validTiles.getGrid()[1][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(7, personalGoalCard7);

        PersonalGoalCard personalGoalCard8 = new PersonalGoalCard();
        personalGoalCard8.validTiles.getGrid()[3][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard8.validTiles.getGrid()[1][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard8.validTiles.getGrid()[5][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard8.validTiles.getGrid()[4][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard8.validTiles.getGrid()[2][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard8.validTiles.getGrid()[0][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(8, personalGoalCard8);

        PersonalGoalCard personalGoalCard9 = new PersonalGoalCard();
        personalGoalCard9.validTiles.getGrid()[4][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard9.validTiles.getGrid()[2][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard9.validTiles.getGrid()[0][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard9.validTiles.getGrid()[3][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard9.validTiles.getGrid()[4][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard9.validTiles.getGrid()[5][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(9, personalGoalCard9);

        PersonalGoalCard personalGoalCard10 = new PersonalGoalCard();
        personalGoalCard10.validTiles.getGrid()[5][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard10.validTiles.getGrid()[3][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard10.validTiles.getGrid()[1][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard10.validTiles.getGrid()[2][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard10.validTiles.getGrid()[0][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard10.validTiles.getGrid()[4][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(10, personalGoalCard10);

        PersonalGoalCard personalGoalCard11 = new PersonalGoalCard();
        personalGoalCard11.validTiles.getGrid()[0][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard11.validTiles.getGrid()[4][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard11.validTiles.getGrid()[2][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard11.validTiles.getGrid()[1][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard11.validTiles.getGrid()[5][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard11.validTiles.getGrid()[3][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(11, personalGoalCard11);

        PersonalGoalCard personalGoalCard12 = new PersonalGoalCard();
        personalGoalCard12.validTiles.getGrid()[1][1] = new Tile(main.java.it.polimi.ingsw.Model.Type.PLANT);
        personalGoalCard12.validTiles.getGrid()[5][0] = new Tile(main.java.it.polimi.ingsw.Model.Type.CAT);
        personalGoalCard12.validTiles.getGrid()[4][4] = new Tile(main.java.it.polimi.ingsw.Model.Type.GAME);
        personalGoalCard12.validTiles.getGrid()[0][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.BOOK);
        personalGoalCard12.validTiles.getGrid()[3][3] = new Tile(main.java.it.polimi.ingsw.Model.Type.TROPHY);
        personalGoalCard12.validTiles.getGrid()[2][2] = new Tile(main.java.it.polimi.ingsw.Model.Type.FRAME);
        validTilesMap.put(12, personalGoalCard12);

    }

    /**
     * This method saves games progress in a file to guarantee persistence funcionality
     * @param filePath: the filePath that points to the file in which to save game progresses.
     */

    public void saveGameProgress(String filePath) {
        FileWriter jsonFile;
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(StrategyCommonGoal.class, new JsonStrategyConverter());
        Gson gson = builder.create();
        gson.serializeNulls();
        File file = new File(filePath);
        try{
            if(file.exists()){
                jsonFile = new FileWriter(filePath,false);
                System.out.println("File opened in overwriting");
            }
            else{
                jsonFile = new FileWriter(filePath,true);
                System.out.println("File opened in append");
            }
            //System.out.println("player playing: "+isPlaying.getNickname());
            //we save the players
            gson.toJson(isPlaying, isPlaying.getClass(), jsonFile);
            jsonFile.close();
            jsonFile = new FileWriter(filePath,true);
            //we save the board
            gson.toJson(board, Board.class, jsonFile);
            //we save the players
            gson.toJson(playersList, playersList.getClass(), jsonFile);
            //we save the commongoals
            //commonGoalCards.stream().forEach(commonGoalCard -> System.out.println(commonGoalCard.getDescription()));
            gson.toJson(commonGoalCards, commonGoalCards.getClass(),jsonFile);
            //we save the flags
            boolean[] flags = {lastTurn, lastRound, gameHasEnded, gameHasStarted};
            gson.toJson(flags, flags.getClass(), jsonFile);
            jsonFile.close();
        }catch(IOException e){
            System.out.println("Error in saving the game progress in json file");
        }



    }

    /**
     * This method is used to guarantee the persistence functionality and is invoked to loadGameProgress from
     * the file in which it was previously saved from method saveGameProgress
     * @param filePath: filPath of the file you want to load game progress from
     * @return: false if the file does not exist, true if the progresses are correctly loaded
     */

    public boolean loadGameProgress(String filePath){
        File jsonFile = new File(filePath);
        if(!jsonFile.exists()){
            System.out.println("the file does not exists");
            return false;
        }
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(StrategyCommonGoal.class, new JsonStrategyConverter());
        Gson gson = builder.create();
        turnCount = 1;
        FileReader fr = null;
        try {
            fr = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("cannot open json file");
            return false;
        }

        JsonReader reader = new JsonReader(fr);
        //reader.setLenient(true);
        isPlaying = gson.fromJson(reader, Player.class);
        //System.out.println("THE NAME OF THE PLAYER IS "+p.getNickname());
        board= gson.fromJson(reader, Board.class);
        //b.printGridMap();
        Type listType = new TypeToken<List<Player>>() {}.getType();
        playersList = gson.fromJson(reader, listType);
        //players.stream().forEach(player->System.out.println(player.getNickname()));
        leaderBoard.addAll(playersList);
        leaderBoard.sort(new scoreComparator());
        fillValidTileMap();
        //playersList.stream().forEach(player->System.out.println(player.getNickname()));
        numOfPlayers = playersList.size();
        iterator = playersList.iterator();
        while (!iterator.next().getNickname().equals(isPlaying.getNickname()));

        Type commongoalType = new TypeToken<List<CommonGoalCard>>(){}.getType();
        commonGoalCards = gson.fromJson(reader, commongoalType);

        boolean[] flags = gson.fromJson(reader, boolean[].class);
        lastTurn = flags[0];
        lastRound = flags[1];
        gameHasEnded = flags[2];
        gameHasStarted = flags[3];
        try {
            fr.close();
        } catch (IOException e) {
            System.out.println("cannot close json file");
        }
        return true;
    }



}

