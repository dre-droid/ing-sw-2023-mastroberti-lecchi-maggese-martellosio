package main.java.it.polimi.ingsw.Server;

import main.java.it.polimi.ingsw.Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Server.Socket.drawInfo;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    Game game;
    private ServerSock serverSock;
    private Server server;
    public boolean isGameBeingCreated = false;

    public Controller(){
    }

    public Controller(Server server){
        this.server = server;

    }
    public ServerSock getServerSock(){
        return this.serverSock;
    }

    public void setServerSock(ServerSock s){
        this.serverSock = s;
    }
    public Controller(ServerSock serverSock){
        this.serverSock = serverSock;
    }

    /**
     * This method is used to create a new instance of game (if there isn't one already running) with numOfPlayers players and then
     * add to the game the player who created the game (nickname). Calls serverRMI method gameIsCreated to notify every waiting ClientRMI
     * that a game has been created
     * @param nickname name of the player who is creating the game
     * @param numOfPlayers number of players that can join the game
     * @return true if the game is created correctly, false if there is already a game running
     */
    public boolean createNewGame(String nickname, int numOfPlayers) throws RemoteException{
        if(game==null){
            /*if(loadGameProgress()){
                System.out.println("Loaded game from file!");
                return false;
            }*/
            game = new Game(numOfPlayers);
            game.addPlayer(nickname);
            server.notifyGameHasBeenCreated();      //server function that notifies socket and rmi that game has been created
            System.out.println("Created new game by "+nickname);
            return true;
        }
        System.out.println("There is already a game to join");
        return false;
    }

    /**
     * this method is used to add a player to the game with the name "nickname"
     * @param nickname name of the player to add to the game
     * @return a numerical code representing the outcome of the operation
     *          (-1) if there is not any game to join
     *          (-2) if the game has already started
     *          (0) if the player joined the game correctly
     */
    public synchronized int joinGame(String nickname) {
        isGameBeingCreated = true;
            if (game == null) {
                //System.out.println("There is no game to join, create a new one " + nickname);
                return -1;
            }
            if (game.hasGameStarted()) {
                //System.out.println("The game has alredy started, " + nickname + " can't join");
                return -2;
            }
            if (game.addPlayer(nickname)) {
                //System.out.println(nickname + " joined the game");
                if (game.hasGameStarted()) {
                    server.serverRMI.notifyStartOfGame();
                    //saveGameProgress();
                    notifyAll();
                }
                return 0;
            }
        return -4;  //should never reach
    }

    /**
     * Removes player with nickname nick from playersList and leaderBoard when client disconnects before game has started.
     * Should only be called if game hasn't started.
     */
    public void removePlayer(String nick) {
        game.removePlayer(nick);
    }

    /**
     * this method is used to check if the game has already started
     * @return true if the game has already started, false otherwise
     */
    public boolean hasGameStarted(){
        if(game!=null){
            return game.hasGameStarted();
        }
        return false;
    }

    /**
     * @return true if first player has successfully joined and created a game
     */
    public boolean hasGameBeenCreated(){
        return game != null;
    }


    /**
     * This method is used to get a copy of the shelf of the player with name "playerNickname" to send to the client so that it can
     * be displayed
     * @param playerNickname name of the player who is requesting the display of his shelf
     * @return a Tile matrix representing the current state of the Shelf of the player, null if the game hasn't started yet
     */
    public Tile[][] getMyShelf(String playerNickname){
        if(game.hasGameStarted()){
            Tile[][] displayGrid = new Tile[6][5];
            for(Player p: game.getPlayerList()){
                if(p.getNickname().equals(playerNickname))
                    displayGrid = p.getShelf().getGridForDisplay();
            }
            return displayGrid;
        }
        return null;
    }

    /**
     * This method is used to get a copy of the board to send it to the client so that it can be displayed
     * @return null if the game hasn't started yet, a matrix o TilePlacingSpot representing the current state of the board if the
     * game has started
     */
    public TilePlacingSpot[][] getTilePlacingSpot(){
        if(game.hasGameStarted()){
            return game.getBoard().getBoardForDisplay();
        }
        return null;
    }

    /**
     * This method is used to get a copy of the board
     */
    public Board getBoard(){
        return new Board(game.getBoard());
    }

    /**
     * This method tells if the player with name "playerNickname" is the one who is playing the turn currently
     * @param playerNickname name of the player
     * @return true if it's playerNickname turn, false otherwise
     */
    public boolean isMyTurn(String playerNickname){
        return game.getIsPlaying().getNickname().equals(playerNickname);
    }

    /**
     * This method is used to get the name of the player who is currently playing the turn
     * @return the name of the player who is currently playing its turn if the game has started, null otherwise
     */
    public String getNameOfPlayerWhoIsCurrentlyPlaying(){
        if(game.hasGameStarted())
            return game.getIsPlaying().getNickname();
        return null;
    }

    /**
     * this method is used to get the description of the first common goal card of the game
     * @return the description of the first common goal card if the game has started, null otherwise
     */
    public String getCommonGoalCard1Description(){
        if(game.hasGameStarted())
            return game.getCommonGoalCards().get(0).getDescription();
        return null;
    }

    /**
     * this method is used to get the description of the second common goal card of the game
     * @return the description of the second common goal card if the game has started, null otherwise
     */
    public String getCommonGoalCard2Description(){
        return game.getCommonGoalCards().get(1).getDescription();
    }

    /**
     * this method is used to get the actual points of the player with the name "playerNickname"
     * @param playerNickname name of the player who is requesting his points
     * @return 0 if the game hasn't started or if the playerNickname is not one of the players in the game, otherwise it returns
     * the points of the player
     */
    public int getPoints(String playerNickname) {
        if(game.hasGameStarted() && game.getPlayerList().stream().filter(player -> player.getNickname().equals(playerNickname)).toList().size()!=0)
            return game.getPlayerList().stream().filter(player -> player.getNickname().equals(playerNickname)).toList().get(0).getScore();
        return 0;
    }


    /**
     * This method is used to draw tiles from the board
     * @param playerNickname the name of the player doing this action
     * @param x the x coordinate of the first tile to draw
     * @param y the y coordinate of the first tile to draw
     * @param amount the amount of tile to draw
     * @param direction the direction in which to draw the tiles
     * @return null if the action is called by a player who is not currently the one who is playing its turn or if the move is not valid
     *         if everything is ok it returns the list of tiles drawn by the player
     */
    public List<Tile> drawFromBoard(String playerNickname, int x, int y, int amount, Board.Direction direction){
        System.out.println("("+x+","+y+") amount = "+amount+" direction ="+direction.toString());
        List<Tile> toBeReturned = new ArrayList<>();
        if(game.getIsPlaying().getNickname().equals(playerNickname)){
            try{
                toBeReturned = game.drawsFromBoard(x,y,amount,direction, playerNickname);
            }catch(Exception e){
                System.out.println("cannot draw these tiles man");
                e.printStackTrace();
                return null;
            }
            return toBeReturned;
        }
        return null;
    }

    /**
     * This method is used to insert the tiles drawn from the board into the shelf
     * @param playerNickname the name of the player doing this action
     * @param tiles the list of tiles to insert into the shelf
     * @param column the column of the shelf into which the tiles must be put
     * @return the outcome of the operation, true if everything is ok, false if the move was not valid or if the player calling thi
     * is not the one playing the current turn
     */
    public boolean insertTilesInShelf(String playerNickname, List<Tile> tiles, int column){
        System.out.println("controller-->insert tiles in shelf"+ tiles.size());
        if(playerNickname.equals(game.getIsPlaying().getNickname())){
            if(column<0 || column>5)
                return false;
            try{
                Player current = getPlayers().stream().filter(player -> player.getNickname().equals(playerNickname)).toList().get(0);
                if(game.insertTilesInShelf(tiles,column,current))
                    return true;
                else
                    return false;
            }catch(InvalidMoveException e){
                return false;
            }
        }
        return false;
    }

    /**
     * this method is used to check if the player with the name "playerNickname" has completed the first common goal
     * @param playerNickname the name of a player
     * @returnm false if the name of the player is not the one of the player who is currently playing its turn or if the player has
     * not completed the first common goal, true if the player has completed the first common goal
     */
    public boolean checkIfCommonGoalN1IsFulfilled(String playerNickname){
        if(playerNickname.equals(game.getIsPlaying().getNickname())){
            return game.checkIfCommonGoalN1IsFulfilled(game.getIsPlaying());
        }
        return false;
    }

    /**
     * this method is used to check if the player with the name "playerNickname" has completed the second common goal
     * @param playerNickname the name of a player
     * @returnm false if the name of the player is not the one of the player who is currently playing its turn or if the player has
     * not completed the second common goal, true if the player has completed the second common goal
     */
    public boolean checkIfCommonGoalN2IsFulfilled(String playerNickname){
        if(playerNickname.equals(game.getIsPlaying().getNickname())){
            return game.checkIfCommonGoalN2IsFulfilled(game.getIsPlaying());
        }
        return false;
    }

    /**
     * This method is used to end the turn, only if the playerNickname is the same of the one of the player who is currently
     * playing its tur
     * @param playerNickname the name of the player who calls this action
     */
    public void endOfTurn(String playerNickname){
        if(hasTheGameEnded()){
            return;
        }
        if(playerNickname.equals(game.getIsPlaying().getNickname())){
            game.endOfTurn(game.getIsPlaying());

            saveGameProgress();
            if(server.serverRMI.isHeARmiPlayer(getNameOfPlayerWhoIsCurrentlyPlaying())){
                if(server.serverRMI.isHeDisconnected(getNameOfPlayerWhoIsCurrentlyPlaying())){
                    System.out.println("skipped disconnected person turn");
                    endOfTurn(getNameOfPlayerWhoIsCurrentlyPlaying());
                }
                server.serverRMI.notifyStartOfTurn(getNameOfPlayerWhoIsCurrentlyPlaying());
            }

        }
    }

    /**
     * this method is used to check if the game has ended
     * @return true if the game has ended, false otherwise
     */
    public boolean hasTheGameEnded(){
        return game.hasTheGameEnded();
    }

    /**
     * this method is used to get the leaderboard
     * @return a list of players representing the leaderboard if the game has started, null otherwise
     */
    public List<Player> getLeaderboard(){
        if(game.hasGameStarted())
            return game.getLeaderBoard();
        return null;
    }

    /**
     * this method is called to perform the turn dynamic in the gameplay.
     * @author Saverio Maggese
     *
     */
    public void playTurn() {
        boolean invalidMoveFlag = false;
        Player thisTurnsPlayer = getPlayers().stream().filter(player -> player.getNickname().equals(game.getIsPlaying().getNickname())).toList().get(0);
        drawInfo info;
        do {
            try {
                info = serverSock.drawInquiry(getNameOfPlayerWhoIsCurrentlyPlaying(), game.getBoard(), game.getIsPlaying().getShelf(), game.getIsPlaying().getPersonalGoalCard(), game.getCommonGoalCards(), getScoringToken(getNameOfPlayerWhoIsCurrentlyPlaying()), getLeaderboard());
                if (!Objects.isNull(info)) {    //null object is passed when player disconnects during turn
                    //System.out.println("Object was passed");
                    game.playTurn(info.getX(), info.getY(), info.getAmount(), info.getDirection(), info.getColumn(), info.getTiles());
                    serverSock.turnEnd(thisTurnsPlayer.getShelf(), thisTurnsPlayer.getNickname());
                    saveGameProgress();
                    server.serverRMI.updateBoard();
                    //server.serverRMI.checkIfCommonGoalsHaveBeenFulfilled(getNameOfPlayerWhoIsCurrentlyPlaying());
                    server.serverRMI.updateCommonGoalTokens();
                    server.serverRMI.updateEndOfTurnObjects(thisTurnsPlayer.getNickname());
                    server.serverRMI.notifyStartOfTurn(getNameOfPlayerWhoIsCurrentlyPlaying());
                    /*while(server.clientsLobby.stream().filter(p->p.getNickname().equals(getNameOfPlayerWhoIsCurrentlyPlaying())).toList().get(0).isDisconnected()){
                        endOfTurn(getNameOfPlayerWhoIsCurrentlyPlaying());
                    }*/
                    invalidMoveFlag = false;
                    if(game.hasTheGameEnded()){
                        System.out.println("Correctly ended game.");
                    }
                }
                else{
                    endOfTurn(getNameOfPlayerWhoIsCurrentlyPlaying());
                }
            } catch (InvalidMoveException e) {
                invalidMoveFlag = true;
                e.printStackTrace();
            }
        }while(invalidMoveFlag);
    }

    /**
     * calls game.endGame() to set endGame to true and notifies RMI and Socket Clients through serverRMI and serverSock methods
     * @throws IOException
     */
    public void endGame() throws IOException {
        server.serverRMI.notifyEndOfGame();
        game.endGame();
        server.serverSock.notifyGameEnd();
        deleteProgress();
    }

    /**
     * Ends the game due to all but one player disconnecting.
     */
    public void disconnectionEndGame(String nicknameOfRemainingPlayer) {
        List<Player> finalLead = getLeaderboard().stream().filter(p -> p.getNickname().equals(nicknameOfRemainingPlayer)).toList();
        server.serverSock.broadcastMessage("[ALLDISCONNECTED]", "Server");
        server.serverRMI.notifyEndOfGame(finalLead, true);
        game.endGame();
        deleteProgress();
    }

    /**
     * this method is used to get the personal goal card for the specified player
     * @param playerNickname name of the specified player
     * @return null if there isn't any player with the name "playerNickname", if there is a player with that name returns its personal goal card
     */
    public Tile[][] getMyPersonalCard(String playerNickname){
        if(game.getPlayerList().stream().noneMatch(player -> player.getNickname().equals(playerNickname)))
            return null;
        return game.getPlayerList().stream().filter(player->(player.getNickname().equals(playerNickname))).toList().get(0).getPersonalGoalCard().getValidTiles().getGridForDisplay();
    }

    /**
     * This method has been made public for testing purposes
     */
    public void saveGameProgress(){
        game.saveGameProgress("src/main/resources/jsonGameProgress/GameProgress.json");
    }

    /**
     * loads GameProgress.json in game if it exists in the directory
     * @return true if GameProgress.json exists and is loaded or false otherwise
     */
    public boolean loadGameProgress(){
        if(checkForSavedGameProgress() && game==null){
            game = new Game("src/main/resources/jsonGameProgress/GameProgress.json");

            return true;
        }
        else
            return false;
    }
    /**
     * This method is used to delete a GameProgress.json file if it exists
     */
    public void deleteProgress(){
        File toBeDeleted = new File("src/main/resources/jsonGameProgress/GameProgress.json");
        if(toBeDeleted.delete())
            System.out.println("File deleted correctly");
        else System.out.println("File not deleted");
    }
    /**
     * This method checks if GameProgress.json is present in this path
     */
    public boolean checkForSavedGameProgress(){
        File toBeChecked = new File("src/main/resources/jsonGameProgress/GameProgress.json");
        if(toBeChecked.exists())
            return true;
        else return false;
    }

    public Map<Integer, PersonalGoalCard> getPGCmap(){
        return game.getValidTilesMap();
    }

    /**
     * Get the personal Goal Card of a player with the nickname passed in as parameter
     * @param playerNickname nickname of the player
     * @return PersonalGoalCard of a given player
     */
    public PersonalGoalCard getPGC(String playerNickname) {
        return game.getPlayerList().stream().filter(p->p.getNickname().equals(playerNickname)).toList().get(0).getPersonalGoalCard();
    }

    public List<CommonGoalCard> getCommonGoalCards(){
        return game.getCommonGoalCards();
    }

    /**
     * this method is used to get the scoring tokens possessed by a player
     * @param playerNickname name of the player
     * @return the list of scoring token of the player if the nickname correspond to the one of a player in the game, null otherwise
     */
    public List<ScoringToken> getScoringToken(String playerNickname){
        Optional<Player> requestingPlayer = game.getPlayerList().stream().filter(player -> player.getNickname().equals(playerNickname))
                .findFirst();
        return requestingPlayer.map(Player::getScoringTokensList).orElse(null);
    }

    /**
     * this method is used to get the scoring tokens of the common goal card passed in the parameter
     * @param commonGoalCard the common goal card of which we want the scoring tokens
     * @return the scoring token of the common goal card if commonGoalCard correspond to one of the common goal cards present in the game
     * null otherwise
     */
    public List<ScoringToken> getAvailableScoringTokens(CommonGoalCard commonGoalCard){
        Optional<CommonGoalCard> opt = game.getCommonGoalCards().stream().filter(cgc->cgc.getDescription().equals(commonGoalCard.getDescription())).findFirst();
        return opt.map(CommonGoalCard::getScoringTokens).orElse(null);
    }

    /**
     *
     * @return a List of String with the nickname of the Players in Game
     */
    public List<String> getGamePlayerListNickname(){
        return game.getPlayerList().stream().map(Player::getNickname).collect(Collectors.toList());
    }

    /**
     *This method is used to get the Game and is meant to be used for testing purposes only.
     */
    public Game getGame() {
        Game copy;
        copy = this.game;
        return copy;
    }

    /**
     * @return the nickname of the player with firstPlayerSeat
     */
    public String getFirstPlayer(){
        for (Player p: game.getPlayerList())
            if (p.hasFirstPlayerSeat()) return p.getNickname();
        return null;
    }

    public int getNumOfPlayers(){
        return game.getNumOfPlayers();
    }

    public List<Player> getPlayers(){
        return game.getPlayerList();
    }

    /**
     * This method is used to check if a player has endGameToken
     * @param nickname nickname of the player
     * @return true if the player with the given nickname has endGameToken, false otherwise
     */
    public boolean hasEndgameToken(String nickname){
        Optional<Player>  p = game.getPlayerList().stream().filter(player -> player.getNickname().equals(nickname)).findFirst();
        if(p.isEmpty())
            return false;
        else
            return p.get().hasEndGameToken();
    }


}
