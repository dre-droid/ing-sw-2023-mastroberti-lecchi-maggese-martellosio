package Server;

import Server.Socket.ServerSock;
import Server.Socket.drawInfo;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import main.java.it.polimi.ingsw.Model.*;

import javax.naming.ldap.Control;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Controller {
    Game game;
    private ServerSock serverSock;

    private Server server;

    public Controller(){
    }

    public Controller(Server server){
        this.server = server;

    }

    public void setServerSock(ServerSock s){
        this.serverSock = s;
    }
    public Controller(ServerSock serverSock){
        this.serverSock = serverSock;
    }

    /**
     * This method is used to create a new instance of game (if there isn't one already running) with numOfPlayers players and then
     * add to the game the player who created the game (nickname)
     * @param nickname name of the player who is creating the game
     * @param numOfPlayers number of players that can join the game
     * @return true if the game is created correctly, false if there is already a game running
     */
    public boolean createNewGame(String nickname, int numOfPlayers){
        if(game==null){
            /*if(loadGameProgress()){
                System.out.println("Loaded game from file!");
                return false;
            }*/
            game = new Game(numOfPlayers);
            game.addPlayer(nickname);
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
     *          (-3) if the chosen nickname is already being used by someone else
     *          (0) if the player joined the game correctly
     */
    public synchronized int joinGame(String nickname) {
        if (game == null) {
            System.out.println("There is no game to join, create a new one " + nickname);
            return -1;
        }
        if (game.hasGameStarted()) {
            System.out.println("The game has alredy started, " + nickname + " can't join");
            return -2;
        }
        if (game.addPlayer(nickname)) {
            System.out.println(nickname + " joined the game");
            return 0;
        }else{
            System.out.println("Nickname already used");
            return -3;
        }
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
    public TilePlacingSpot[][] getBoard(){
        if(game.hasGameStarted()){
            return game.getBoard().getBoardForDisplay();
        }
        return null;
    }

    /**
     * This method tells if the player with name "playerNickname" is the one who is playing the turn currently
     * @param playerNickname name of the player
     * @return true if it's playerNickname turn, false otherwise
     */
    public boolean isMyTurn(String playerNickname){
        return game.isPlaying.getNickname().equals(playerNickname);
    }

    /**
     * This method is used to get the name of the player who is currently playing the turn
     * @return the name of the player who is currently playing its turn if the game has started, null otherwise
     */
    public String getNameOfPlayerWhoIsCurrentlyPlaying(){
        if(game.hasGameStarted())
            return game.isPlaying.getNickname();
        return null;
    }

    /*public boolean hasGameBeenCreated(){
        return game!=null;
    }*/

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
        List<Tile> toBeReturned = new ArrayList<>();
        if(game.isPlaying.getNickname().equals(playerNickname)){
            try{
                toBeReturned = game.drawsFromBoard(x,y,amount,direction, playerNickname);
            }catch(Exception e){
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
        if(playerNickname.equals(game.isPlaying.getNickname())){
            if(column<0 || column>5)
                return false;
            try{
                if(game.insertTilesInShelf(tiles,column,game.isPlaying))
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
        if(playerNickname.equals(game.isPlaying.getNickname())){
            return game.checkIfCommonGoalN1IsFulfilled(game.isPlaying);
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
        if(playerNickname.equals(game.isPlaying.getNickname())){
            return game.checkIfCommonGoalN2IsFulfilled(game.isPlaying);
        }
        return false;
    }

    /**
     * This method is used to end the turn, only if the playerNickname is the same of the one of the player who is currently
     * playing its tur
     * @param playerNickname the name of the player who calls this action
     */
    public void endOfTurn(String playerNickname){
        if(playerNickname.equals(game.isPlaying.getNickname())){
            game.endOfTurn(game.isPlaying);
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

    public Player getFirstPlayer(){
        for (Player p: game.getPlayerList())
            if (p.hasFirstPlayerSeat()) return p;
        return null;
    }

    /**
     * this method is called
     * @author Saverio Maggese
     *
     */

    public void playTurn() throws InvalidMoveException {
        drawInfo info;
        boolean flag;
        do {
            flag = false;
            info = serverSock.drawInquiry(this.getNameOfPlayerWhoIsCurrentlyPlaying(),game.getBoard(),game.getIsPlaying().getShelf(), game.getIsPlaying().getPersonalGoalCard(), game.getCommonGoalCards(), this.getLeaderboard());
            try{
                game.playTurn(info.getX(),info.getY(),info.getAmount(),info.getDirection(),info.getColumn(), info.getOrder());
            }catch (InvalidMoveException e) {
                flag = true;
                try {
                    serverSock.printErrorToClient("Invalid choice! Choose another tile.", getNameOfPlayerWhoIsCurrentlyPlaying());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }while(flag);

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

    public void saveGameProgress() throws IOException {
        Gson gson = new Gson();
        gson.toJson(game, new FileWriter("MyShelfie/src/Server/GameProgress.json"));
    }

    public boolean loadGameProgress(){
        Gson gson =  new Gson();
        try{
            JsonReader reader = new JsonReader(new FileReader("MyShelfie/src/Server/GameProgress.json"));
            game = gson.fromJson(reader, Game.class);
            return true;
        }catch(IOException ex){
            return false;
        }
    }

}
