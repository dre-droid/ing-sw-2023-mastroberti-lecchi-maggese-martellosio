package main.java.it.polimi.ingsw.GUI;

import main.java.it.polimi.ingsw.Server.RMI.ClientNotificationInterfaceRMI;
import main.java.it.polimi.ingsw.Server.RMI.RMIinterface;
import javafx.application.Platform;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ClientNotificationRMIGUI extends java.rmi.server.UnicastRemoteObject implements ClientNotificationInterfaceRMI {
    private RMIinterface serverRMI;

    private boolean quit;

    GameSceneController gsc;

    private String serverIp;

    private int port;

    private String nickname;
    private boolean MyTurnFlag;
    private boolean EndGameFlag;
    private boolean GameStartFlag;

    private List<Tile> drawnTiles;
    public int  joinGameOutcome = -5;

    private String myIp;

    private List<CommonGoalCard> commonGoalCards;

    public boolean cannotContactServer;

    public ClientNotificationRMIGUI(String serverIp) throws RemoteException{
        this.serverIp = serverIp;
        MyTurnFlag = false;
        EndGameFlag = false;
        GameStartFlag = false;
    }


    /**
     * this method is used to send a request to the server rmi to get the leaderboard
     * @return a list of player representing the leaderboard, null if there are some problems
     * in contatting the server
     */
    public List<Player> getLeaderboard(){
        try{
            return serverRMI.getLeaderboard();
        }catch(RemoteException remoteException){
            backToLogin();
            return null;
        }
    }

    /**
     * this method is used to reconnect to a game
     * @return true if the reconnection goes well, false otherwise
     */
    public boolean reconnectToGame(){
        System.out.println("Trying to reconnect...");
        try{
            boolean reconnected = serverRMI.reconnect(nickname,port,myIp);
            periodicPing();
            return reconnected;
        }catch(RemoteException re){
            return false;
        }
    }


    /**
     * this method is used to start the client notification server by connecting to the server rmi
     * @throws RemoteException
     */
    public void startNotificationServer() throws RemoteException{
        try{
            Registry registryServer = LocateRegistry.getRegistry(serverIp);
            serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");
            cannotContactServer = false;
        }catch(RemoteException | NotBoundException e){
            System.out.println("ClientNotificationRMIGUI--> ERROR: cannot connect to server");
            backToLogin();
        }
        //ip address
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            myIp = inetAddress.getHostAddress();
            //System.out.println("my ip is = "+myIp);
        } catch (UnknownHostException e) {
            System.out.println("cannot get ip address ");
        }
        MyTurnFlag = false;
        EndGameFlag = false;
        GameStartFlag = false;
        Random random = new Random();
        port = random.nextInt(3000,6000);
        Registry clientRegistry = LocateRegistry.createRegistry(port);
        clientRegistry.rebind("Client",this);
    }

    /**
     * this method is used to set the chosen nickname
     * @param nick nickname chosen by the player
     */
    public void setnickname(String nick){
        this.nickname = nick;
    }

    /**
     * this method is used to get the nickname
     * @return nickname
     */
    public String getNickname(){return this.nickname;}

    public int joinGame() {
        System.out.println("trying login");
        try {
            return serverRMI.joinGame(nickname, port, myIp);
        } catch (RemoteException e) {
            backToLogin();
            return -3;
        }
    }

    /**
     * this method is used to join the lobby
     * @return a int code value representing the outcome of the operation (for the explanation of
     * the numeric value look at the sequence diagrams)
     */
    public int joinLobby() {
        try {
            return serverRMI.joinLobby(nickname, port, myIp);
        } catch (RemoteException e) {
            backToLogin();
            return -3;
        }
    }

    /**
     * this method is useed to constantly ping the rmi server to check if the server is still up, in ù
     * case the server does not respond send the player back to the login scene
     */
    public void periodicPing() {
        new Thread(() -> {
            while (!cannotContactServer) {
                try {
                    serverRMI.setLastPing(nickname);
                    if(quit){
                        Platform.runLater(Platform::exit);
                        System.exit(0);
                    }


                } catch (RemoteException e) {
                    backToLogin();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * this method is used to send the create new game command to the server
     * @param numOfPlayers
     * @return
     */
    public boolean createNewGame(int numOfPlayers){
        System.out.println("creating new game...");
        try {
            return serverRMI.createNewGame(nickname, numOfPlayers, port, myIp);
        } catch (RemoteException e) {
            backToLogin();
            return false;
        }
    }



    @Override
    public void gameJoinedCorrectlyNotification() throws RemoteException {

    }

    @Override
    public void problemInJoiningGame(String problem) throws RemoteException {

    }

    @Override
    public void gameCreatedCorrectly() throws RemoteException {

    }

    @Override
    public void cannotCreateNewGame(String problem) throws RemoteException {

    }

    @Override
    public void someoneJoinedTheGame(String nickname) throws RemoteException {

    }
    public void gameHasBeenCreated() throws RemoteException{

    }

    /**
     * this method is called by the server to notify the client that the game has started
     * @param startingPlayer name of the player who holds the first player seat
     * @throws RemoteException
     */
    @Override
    public void startingTheGame(String startingPlayer) throws RemoteException {
        GameStartFlag=true;
        System.out.println("GAME HAS STARTED LETSGOOOOOOO");
        updateGUIAtBeginningOfGame();
    }

    /**
     * this method is used to update the gui at the beginning of the game :)
     */
    public void updateGUIAtBeginningOfGame(){
        try{
            TilePlacingSpot[][] board= serverRMI.getBoard();
            Map<Integer, PersonalGoalCard> pgcMap = serverRMI.getPGCmap();
            PersonalGoalCard pgc = serverRMI.getPGC(nickname);
            commonGoalCards = serverRMI.getCommonGoalCards();
            List<Player> leaderboard = serverRMI.getLeaderboard();
            String isPlaying = serverRMI.getIsPlaying();

            if(gsc!=null) {
                if(serverRMI.getFirstPlayerToPlay().equals(nickname)){
                    gsc.setFirstPlayerSeat();
                }
                System.out.println("gsc non è null per "+nickname);
                gsc.updateGUIAtBeginningOfGame(board, pgcMap, pgc, commonGoalCards, leaderboard, isPlaying);
                if(serverRMI.haveIEndGameToken(nickname))
                    gsc.setEndGameToken();

            }
            else{
                System.out.println("gsc null ma non si sa perchè");
            }
        }catch(RemoteException re){
            backToLogin();
        }
    }

    /**
     * this method is used to send the command to the server to draw the tiles from the board
     * @param x x coordinate on the board
     * @param y y coordinate on the board
     * @param amount amount of tiles to draw
     * @param direction direction in which to draw the tiles
     * @return the list of drawn tiles if the operation didn't contain any errors, null otherwise
     */
    public boolean drawTilesFromBoard(int x, int y, int amount, Board.Direction direction){
        try {
            System.out.println("("+x+","+y+") amount = "+amount+" direction ="+direction.toString());
            drawnTiles = serverRMI.drawTilesFromBoard(nickname, x, y, amount, direction);
            return  drawnTiles!=null;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * this method is used to switch the position of two of the drawn tilies
     * @param pos1 position of the first tile
     * @param pos2 position of the second tile
     */
    public void rearrangeDrawnTiles(int pos1, int pos2){
        try{
            Tile temp = drawnTiles.get(pos1);
            drawnTiles.set(pos1, drawnTiles.get(pos2));
            drawnTiles.set(pos2, temp);
        }catch (IndexOutOfBoundsException e){
            return;
        }
    }

    /**
     * this method is used to send the command to insert the drawn tiles in the shelf
     * @param column column in which to insert the drawn tiles
     * @return true if the tiles are inserted, false otherwise
     */
    public boolean insertTilesInShelf(int column ) {
        try {
            boolean returnValue = serverRMI.insertTilesInShelf(nickname,drawnTiles,column);
            if(returnValue){
                drawnTiles = null;
                MyTurnFlag = false;
            }
            return returnValue;

        } catch (RemoteException e) {
            backToLogin();
            return false;
        }
    }

    /**
     * this method is used to get the shelf of the player from the server rmi
     * @return a matrix of tiles representing the shelf of the requesting player, null if there
     * are problems in contacting the server or if the name of the player is not one of the players
     * in the game
     */
    public Tile[][] getMyShelf(){
        try {
            return serverRMI.getMyShelf(nickname);
        } catch (RemoteException e) {
            backToLogin();
            return null;
        }
    }

    /**
     * this method is used to get the shelf of an opponent player
     * @param player name of the opponent player
     * @return a matrix of tiles representing the shelf of the requesting player, null if there
     * are problems in contacting the server or if the name of the player is not one of the players
     * in the game
     */
    public Tile[][] getShelfOfPlayer(String player){
        try {
            return serverRMI.getMyShelf(player);
        } catch (RemoteException e) {
            backToLogin();
            return null;
        }
    }

    /**
     * this method is called by the rmi server to notify the players that someone has completed
     * a common goal
     * @param playerNickname name of the player who completed a common goal
     * @param cgc common goal that has been completed
     * @throws RemoteException
     */
    @Override
    public void someoneHasCompletedACommonGoal(String playerNickname, CommonGoalCard cgc) throws RemoteException {
        if(playerNickname.equals(this.nickname)) {
            List<ScoringToken> tokens = serverRMI.getMyTokens(this.nickname);
            //update the view
            this.gsc.updateScoringTokens(tokens);
        }
        int nOfCommonGoalCard=-1;
        for(CommonGoalCard c: commonGoalCards){
            if(cgc.getDescription().equals(c.getDescription())){
                nOfCommonGoalCard = commonGoalCards.indexOf(c);
            }
        }
        System.out.println("common goal number: "+nOfCommonGoalCard);
        if(nOfCommonGoalCard==0){
            for(ScoringToken st: serverRMI.getCgcTokens(cgc)){
                System.out.println(st.getPoints());
            }
            this.gsc.updateCommonGoalCardTokens(1,serverRMI.getCgcTokens(cgc));
        }else if (nOfCommonGoalCard==1){
            this.gsc.updateCommonGoalCardTokens(2,serverRMI.getCgcTokens(cgc));
        }
        else{
            System.out.println("errore -1");
        }
    }

    /**
     * this method is called by the server to notify that a player ended its turn
     * @param currentPlayerNickname name of the player that ended the turn
     * @param nextPlayerNickname name of the next player that has to play
     * @throws RemoteException
     */
    @Override
    public void aTurnHasEnded(String currentPlayerNickname, String nextPlayerNickname) throws RemoteException {
        gsc.updateTurnLabel(serverRMI.getIsPlaying());
        gsc.updateLeaderboard(serverRMI.getLeaderboard());
        gsc.updateBoard(serverRMI.getBoard());
        List<Player> players = serverRMI.getPlayers();
        if(players.stream().anyMatch(Player::hasEndGameToken)){
            if(this.gsc!=null){
                gsc.setOpponentEndGameToken(players.stream().filter(player -> player.hasEndGameToken()).findFirst().get().getNickname());
            }
        }
        if(serverRMI.haveIEndGameToken(nickname))
            gsc.setEndGameToken();
    }

    /**
     * this method is called by the server to notify the players that the game is over
     * @param leaderboard leaderboard containing the players and their score
     * @throws RemoteException
     */
    @Override
    public void gameIsOver(List<Player> leaderboard) throws RemoteException {
        if(leaderboard==null)
            return;
        gsc.switchToEndGameScene(leaderboard);
    }

    @Override
    public void moveIsNotValid() throws RemoteException {

    }

    @Override
    public void announceCommonGoals(String commonGoals) throws RemoteException {
        //alert to tell players who completed the common goal
    }

    @Override
    public void runOutOfTime() throws RemoteException {

    }

    @Override
    public void ping() throws RemoteException {

    }

    /**
     * this method is called by the server to notify the client that his turn has started
     * @throws RemoteException
     */
    @Override
    public void startTurn() throws RemoteException {
        gsc.updateTurnLabel(serverRMI.getIsPlaying());
        MyTurnFlag = true;
    }

    /**
     * this method is called by the server to show on the chat a message
     * @param text body of the message
     * @param sender name of the player who sent the message
     * @param pm boolean value, if true it means that this is a private message
     * @throws RemoteException
     */
    @Override
    public void receiveMessage(String text, String sender, Boolean pm) throws RemoteException {
        if(pm)
            gsc.rmiMessageTextArea("[MESSAGE FROM " + sender + " TO YOU]: " + text);
        else
            gsc.rmiMessageTextArea("[MESSAGE FROM "+sender+"]: "+text);
    }


    /**
     * this message is used to send a message
     * @param message the message
     */
    public void sendChatMessage(String message){
        String text = "", receiver = "";
        int atIndex;
        try{
            if(message.startsWith("@")){
                receiver = message.substring( 1);
                atIndex = receiver.indexOf(' ');
                text = receiver.substring(atIndex + 1);
                receiver = receiver.substring(0, atIndex);
                if(!Objects.equals(receiver, nickname))
                    serverRMI.chatMessage(nickname, text, receiver, true);
            }
            else {
                receiver = "all";
                serverRMI.chatMessage(nickname, message, receiver, false);
            }
        }catch(RemoteException re){
            backToLogin();
        }

    }

    /**
     * this method is used to send the quit command to the server
     */
    public void quitGame(){
        try {
            this.quit = true;
            serverRMI.quitGame(nickname);
        } catch (IOException e) {
            backToLogin();
        }
    }

    @Override
    public void invalidCommandSent() throws RemoteException {

    }

    /**
     * this method is called by the server to update the view of the board of the client
     * @param boardView a matrix of tile placing spot representing the board
     * @throws RemoteException
     */
    @Override
    public void updateBoard(TilePlacingSpot[][] boardView) throws RemoteException {
        System.out.println(nickname+" has updated the board");
        gsc.updateBoard(boardView);
    }

    /**
     * this method is called by the server to update the view of this client of the opponent's shelf
     * @param nickname name of the opponent player
     * @param grid matrix of tiles representing the shelf of the opponent player
     * @throws RemoteException
     */
    @Override
    public void updateOppShelf(String nickname, Tile[][] grid) throws RemoteException {
        gsc.updateOppShelf(nickname,grid);
    }

    /**
     * this method is used to set the game scene controller
     * @param controller game scene controller
     */
    public void setGameSceneController(GameSceneController controller){
        System.out.println("controller set");
        gsc = controller;
        if(gsc!=null){
            System.out.println("gsc non è nullo nel set");
        }
    }

    /**
     * this method is used to get the board from the server
     * @return a matrix of tile placing spot representing the board, null if there are some
     * problems in contacting the rmi server
     */
    public TilePlacingSpot[][] getBoard(){
        try {
            return serverRMI.getBoard();
        } catch (RemoteException e) {
            backToLogin();
            return null;
        }
    }

    /**
     * this method is used to check on the server if the game has already started
     * @return true if the game has started, false if it has not started or if there are problems
     * in contacting the server
     */
    public boolean hasGameStarted(){
        try {
            return serverRMI.hasGameStarted();
        } catch (RemoteException e) {
            backToLogin();
            return false;
        }
    }

    /**
     * this method is used to check the value of the MyTurnFlag
     * @return the value of the MyTurnFlag
     */
    public boolean isMyTurn(){
        return MyTurnFlag;
    }
    public void nickNameAlreadyInUse() throws RemoteException{

    }
    public void notifyOfDisconnection() throws RemoteException{

    }

    /**
     * this method is called by the server to notify the client of the outcome of the game join
     * @param outcome outcome of the join operation
     * @throws RemoteException
     */
    public void joinGameOutcome(int outcome) throws RemoteException{
        joinGameOutcome = outcome;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * this method is used to check if the game is being created by another player
     * @return true if someone else is creating the game, false otherwise
     */
    public boolean isGameBeingCreated() {
        try {
            return serverRMI.isGameBeingCreated();
        } catch (RemoteException e) {
            backToLogin();
            return false;
        }
    }

    /**
     * this method is used to check if the player with the chosen nickname is the first in the
     * lobby
     * @param nickname name of the player
     * @return true if the player is the first in the lobby, false otherwise
     */
    public boolean firstInLobby (String nickname){
        try {
            return serverRMI.firstInLobby(nickname);
        } catch (RemoteException e) {
            backToLogin();
            return false;
        }
    }

    /**
     * this method is called by the server to send a broadcast message
     * @param message content of the message
     * @throws RemoteException
     */
    @Override
    public void broadcastedMessage(String message) throws RemoteException {
        if(!Objects.isNull(gsc))
            gsc.rmiMessageTextArea(message);
    }

    /**
     * this method is called by the server to update the view of the scoring tokens on each of
     * the common goal cards
     * @throws RemoteException
     */
    @Override
    public void updateCommonGoalTokens() throws RemoteException {
        List<CommonGoalCard> cgcs = serverRMI.getCommonGoalCards();
        gsc.updateCommonGoalCardTokens(1, cgcs.get(0).getScoringTokens());
        gsc.updateCommonGoalCardTokens(2, cgcs.get(1).getScoringTokens());
    }

    /**
     * this method is used to get the scoring token of the player
     * @return the scoring token of the player, null if there are problems in contacting the
     * server
     */
    public List<ScoringToken> getMyToken(){
        try {
            return serverRMI.getMyTokens(nickname);
        } catch (RemoteException e) {
            backToLogin();
            return null;
        }
    }

    /**
     * this method is used to update the view of the opponents shelves
     */
    public void updateOpponentsShelf(){
        try {
            List<Player> players = serverRMI.getPlayers();
            for(Player p: players){
                updateOppShelf(p.getNickname(), p.getShelf().getGridForDisplay());
            }
        } catch (RemoteException e) {
            backToLogin();
        }
    }

    /**
     * this method is used to go back to the login scene
     */
    public void backToLogin(){
        cannotContactServer = true;
        this.gsc.backToLogin();
    }

    /**
     * this method is used to check if the player with the chosen nickname has the end game
     * token
     * @param nickname name of the chosen player
     * @return true if the player has the end game token, false if he hasn't got it or if there
     * are problems in contacting the server
     */
    public boolean hasEndgameToken(String nickname){
        try {
            return serverRMI.haveIEndGameToken(nickname);
        } catch (RemoteException e) {
            backToLogin();
            return false;
        }
    }
}
