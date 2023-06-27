package main.java.it.polimi.ingsw.Server.RMI;

import main.java.it.polimi.ingsw.Server.Controller;
import main.java.it.polimi.ingsw.Server.Server;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Server.ClientInfoStruct;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

//TODO client will receive chat messages from socket clients while a game hasn't started yet, but won't send any until the game started, the previous messages will appear
public class ServerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{
    
    Server server;
    Controller controller;
    private final long DISCONNECTION_TIME = 5000;  //disconnection threshold: 5s
    private final int drawDelay= 60000;
    private final int insertDelay = 70000;
    //private List<ClientNotificationRecord> clients;

    private Map<String, ClientNotificationInterfaceRMI> clients;
    private Map<ClientNotificationInterfaceRMI, RmiNickStruct> clientsLobby;

    public ServerRMI(Controller controller, Server server) throws RemoteException {super();
        clients = new HashMap();
        clientsLobby = new HashMap<>();
        //timerDraw = new Timer();
        //timerInsert = new Timer();
        this.controller = controller;
        this.server = server;
        //checkForDisconnections();

    }

    /**
     *This method is called by ClientRMI to join a map Lobby, used to alert every ClientRMI waiting for a game to be created
     * @author Diego Lecchi
     * @param nickname name of the player that wants to join the lobby
     * @param port used to connect to the rmi client
     * @return 0 if the player has been added to the map, -2 if something goes wrong with reconnect, -3 if the game has been created
     * and the nickname does not correspond to one of the players in the game who are disconnected
     * @throws RemoteException
     */
    @Override
    public int joinLobby(String nickname, int port, String ip) throws RemoteException{
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(ip, port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        for(int i = 0; i < server.clientsLobby.size(); i++)                 //search for equal nicknames in server.clientsLobby arrayList
            if(nickname.equals(server.clientsLobby.get(i).getNickname())){
                clientToBeNotified.nickNameAlreadyInUse();
                return -1;
            }
        server.clientsLobby.add(new ClientInfoStruct(nickname));            //adds object ClientInfoStruct to server.clientsLobby arrayList

        for(int i = 0; i < server.clientsLobby.size(); i++)                 //sets rmiPort for the ClientInfoStruct object with the same nickname in
            if(nickname.equals(server.clientsLobby.get(i).getNickname())){  //server.clientsLobby arrayList
                server.clientsLobby.get(i).setRmiPort(port);
                server.clientsLobby.get(i).setRmiIp(ip);
            }
        server.notifyServer();                            //notifies server that a new client has been added to arrayList server.clientsLobby
        clientsLobby.put(clientToBeNotified, new RmiNickStruct(nickname));
        clientsLobby.get(clientToBeNotified).setLastPing(System.currentTimeMillis());
        checkForDisconnectionsV2(clientsLobby.get(clientToBeNotified));
        return 0;
    }

    //TODO gestire se più giocatori joinano rispetto a numero partita
    /**
     * This method is called to join the game, if the controller added the player correctly it alerts the client and add it to the
     * clients list, if the controller cannot add the player to the game (outcome=-1,-2,-3) it sends a message to the client with the
     * corresponding error
     * if the game has already started, or it has been loaded from file it will do a reconnect instead of a join
     * @param nickname name of the player that wants to join the game
     * @param port used to connect to the rmi client
     * @return 0 if the player has been added to the game, -1, -2, -3 if there has been an error in the joining of the game
     *          -4 means problems in reconnecting
     * @throws java.rmi.RemoteException
     */
    @Override
    public int joinGame(String nickname,int port,String ip) throws java.rmi.RemoteException {
        int outcome;
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(ip, port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        outcome = controller.joinGame(nickname);
        switch (outcome){
            case 0: {
                clientToBeNotified.gameJoinedCorrectlyNotification();
                clients.put(nickname, clientToBeNotified);
                server.addPlayerToRecord(nickname, Server.connectionType.RMI);
                server.broadcastMessage("Player " + nickname + " joined the game!", nickname);
                /*for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    if(client.getValue()!=null)
                        client.getValue().someoneJoinedTheGame(nickname);
                }

                 */
                if (controller.hasGameStarted()) {
                    notifyStartOfGame();
                }
            }break;
            case -1: {
                clientToBeNotified.problemInJoiningGame("There is no game to join");
            }break;
            case -2: {
                clientToBeNotified.problemInJoiningGame("Sorry, the game has already started");
            }break;
        }

        clientToBeNotified.joinGameOutcome(outcome);
        return outcome;
    }

    /**
     * this method is used to check if the game is being created
     * @return true if the game is being created by someone else, false otherwise
     * @throws RemoteException
     */
    public boolean isGameBeingCreated() throws RemoteException{
        return controller.isGameBeingCreated;
    }

    /**
     * this method is used to check if the selected player is the first in the lobby
     * @param nickname name of the player
     * @return true if the player is the first of the lobby, false otherwise
     * @throws RemoteException
     */
    public boolean firstInLobby (String nickname) throws RemoteException{
        return server.clientsLobby.get(0).getNickname().equals(nickname);
    }

    /**
     * this method is called by Controller in createNewGame method when a new game is created. Calls method gameHasBeenCreated
     * for every RMI client in clientsLobby Map
     * @author Diego Lecchi
     * @throws RemoteException
     */
    public void gameIsCreated() throws RemoteException{
        for (Map.Entry<ClientNotificationInterfaceRMI, RmiNickStruct> client : clientsLobby.entrySet()) {
            client.getKey().gameHasBeenCreated();
        }
    }



    /**
     * This method is called to create a new game that can host a number of player equals to numOfPlayers
     * @param nickname name of the player that is creating the match
     * @param numOfPlayers parameter indicating the number of players that can join the created game
     * @param port of the rmi client
     * @return true if the game is created correctly, false if there are any problems and the game is not created
     * @throws java.rmi.RemoteException
     */
    @Override
    public boolean createNewGame(String nickname, int numOfPlayers,int port, String ip) throws java.rmi.RemoteException{
        if(numOfPlayers<0 || numOfPlayers>4){
            System.out.println("Wrong parameters");
            return false;
        }

        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(ip,port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if(controller.createNewGame(nickname, numOfPlayers)){
            clientToBeNotified.gameCreatedCorrectly();
            //clients.add(new ClientNotificationRecord(nickname,clientToBeNotified));
            clients.put(nickname, clientToBeNotified);
            server.addPlayerToRecord(nickname, Server.connectionType.RMI);
            //System.out.println("Created new game by "+nickname);
            return true;
        }
        clientToBeNotified.cannotCreateNewGame("There is already a game to join");
        System.out.println("There is already a game to join");
        return false;
    }


    /**
     * This method is used to draw the tiles from the board
     * @param playerNickname name of the player doing this action
     * @param x x coordinate on the board
     * @param y y coordinate on the board
     * @param amount number of tiles to draw
     * @param direction direction in which to draw
     * @return the list of tiles drawn if the move is valid, if the move is not valid it returns null
     * @throws java.rmi.RemoteException
     */
    @Override
    public List<Tile> drawTilesFromBoard(String playerNickname, int x,int y,int amount,Board.Direction direction) throws java.rmi.RemoteException{
        System.out.println("("+x+","+y+") amount = "+amount+" direction ="+direction.toString());
        // check that a column that can fit 'amount' tiles exists in player's shelf
        boolean tooManyTilesDrawn = true;
        for (int i = 0; i < 5; i++) {
            if (new Shelf(getMyShelf(playerNickname)).canItFit(amount, i))
                tooManyTilesDrawn = false;
        }
        if (tooManyTilesDrawn) return null;

        List<Tile> drawnTiles = controller.drawFromBoard(playerNickname, x, y, amount, direction);
        if(drawnTiles==null){
            System.out.println("move is not valid");
            clients.get(playerNickname).moveIsNotValid();
            return null;
        }
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            if(client.getValue()!=null && (server.clientsLobby.stream().noneMatch(cis -> cis.getNickname().equals(client.getKey()) && cis.isDisconnected()))){
                client.getValue().updateBoard(controller.getTilePlacingSpot());
            }
        }
        return drawnTiles;
    }

    /**
     * Updates all RMI clients' boards with the controller's version
     */
    public void updateBoard(){
        try {
            for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
                if (client.getValue() != null) {
                    client.getValue().updateBoard(controller.getTilePlacingSpot());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Updates all RMI clients game objects at the end of the turn
     * @param playerNickname - the name of the player whose turn just ended
     */
    public void updateEndOfTurnObjects(String playerNickname){
        try {
            for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
                if (client.getValue() != null) {
                    client.getValue().aTurnHasEnded(playerNickname, controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                    client.getValue().updateOppShelf(playerNickname, controller.getMyShelf(playerNickname));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * this method is used to check if the game has started
     * @return true if the game has started, false otherwise
     * @throws RemoteException
     */
    @Override
    public boolean hasGameStarted() throws RemoteException {
        return controller.hasGameStarted();
    }


    /**
     * this method is used to get a copy of the shelf of the player with the nickname passed in the parameter
     * @param playerNickname name of the player
     * @return a matrix of tile representing the shelf of the player
     * @throws RemoteException
     */
    @Override
    public Tile[][] getMyShelf(String playerNickname) throws RemoteException {
        return controller.getMyShelf(playerNickname);
    }


    /**
     * This method is used to check if it is someone's turn
     * @param playerNickname name of the player
     * @return true if it's the turn of the player with nickname equal to playerNickname, false otherwise
     * @throws RemoteException
     */
    public boolean isMyTurn(String playerNickname) throws RemoteException{
        return controller.isMyTurn(playerNickname);
    }

    /**
     * this method is called by the clients to get a representation of the actual board
     * @return a matrix of tile placing spots representing the actual state of the board
     * @throws RemoteException
     */
    @Override
    public TilePlacingSpot[][] getBoard() throws RemoteException {
        return controller.getTilePlacingSpot();
    }

    /**
     * this method is called by the clients to do the operation of inserting the tiles in their shelf
     * @param playernickName name of the player doing the action
     * @param tiles array of tiles to insert in the shelf
     * @param column number of the column where the tiles have to be inserted
     * @return true if the operation is valid, false otherwise
     * @throws RemoteException
     */
    @Override
    public boolean insertTilesInShelf(String playernickName, List<Tile> tiles, int column) throws RemoteException{
        if(controller.insertTilesInShelf(playernickName, tiles, column)){
            //timerInsert.cancel();
            System.out.println("ServerRMI-->tiles correctly inserted");
            for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                if(!client.getKey().equals(playernickName)){
                    if(client.getValue()!=null && (server.clientsLobby.stream().noneMatch(cis -> cis.getNickname().equals(client.getKey()) && cis.isDisconnected())))
                        client.getValue().updateOppShelf(playernickName, controller.getMyShelf(playernickName));
                }
            }
            endOfTurn(playernickName);
            return true;
        }
        else {
            System.out.println("ServerRMI-->problem in inserting tiles");
            clients.get(playernickName).moveIsNotValid();
            //clients.stream().filter(cl->cl.nickname.equals(playernickName)).toList().get(0).client.moveIsNotValid();
            return false;
        }
    }


    /**
     * this method is used to check if the player has completed a common goal, if he has it notifies the clients
     * @param playerNickname name of the player
     * @throws RemoteException
     */
    public void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())) {
            if (controller.checkIfCommonGoalN1IsFulfilled(playerNickname)) {

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    if(client.getValue()!=null && (server.clientsLobby.stream().noneMatch(cis -> cis.getNickname().equals(client.getKey()) && cis.isDisconnected())))
                        client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCards().get(0));
                }
            }
            if (controller.checkIfCommonGoalN2IsFulfilled(playerNickname)){

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    if(client.getValue()!=null && (server.clientsLobby.stream().noneMatch(cis -> cis.getNickname().equals(client.getKey()) && cis.isDisconnected())))
                        client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCards().get(1));
                }
            }

        }
    }


    /**
     * Method is called after RMI client's turn is over. All other RMI clients are notified, and controller.endOfTurn() is invoked.
     * @param playerNickname the player whose turn is ending
     * @throws RemoteException
     */
    public void endOfTurn(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())){
            if(controller.hasTheGameEnded()){
                System.out.println("notifying end of game");
                notifyEndOfGame();
            }
            checkIfCommonGoalsHaveBeenFulfilled(playerNickname);
            controller.endOfTurn(playerNickname);
            server.notifySocketOfTurnEnd();
            for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                if(client.getValue()!=null && (server.clientsLobby.stream().noneMatch(cis -> cis.getNickname().equals(client.getKey()) && cis.isDisconnected())))
                    client.getValue().aTurnHasEnded(playerNickname, controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            }
        }
    }


    /**
     * this method is called by the clients to get their actual score
     * @param playerNickname name of the player requesting its score
     * @return the actual score of the player
     * @throws RemoteException
     */
    @Override
    public int getPoints(String playerNickname) throws RemoteException {
        return controller.getPoints(playerNickname);
    }

    /**
     * this game is used by the clients to check if the game is over
     * @return true if the game is over
     * @throws RemoteException
     */
    @Override
    public boolean isGameOver() throws RemoteException {
        return controller.hasTheGameEnded();
    }

    /**
     * this method is used by a rmi client to reconnect to the game, the player can reconnect if his name matches one of the disconnected
     * players
     * @param playerNickname name of the player who is trying to reconnect
     * @param port port of the client
     * @param ip ip address of the client
     * @return true if the player reconnected to the game, false otherwise
     * @throws RemoteException
     */
    @Override
    public boolean reconnect(String playerNickname, int port, String ip)  throws RemoteException{
        //we check if there is a player with this name in the game
        for(ClientInfoStruct cis: server.clientsLobby){
            if(cis.getNickname().equals(playerNickname) && !cis.isDisconnected())
                System.out.println("problema è che il player risulta ancora connesso");
            if(cis.getNickname().equals(playerNickname) && cis.isDisconnected()){
                cis.setRmiIp(ip);
                cis.setRmiPort(port);
                cis.setDisconnected(false);
                ClientNotificationInterfaceRMI clientToBeNotified;
                try{
                    Registry registry = LocateRegistry.getRegistry(ip, port);
                    clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
                } catch (NotBoundException e) {
                    return false;
                }
                clients.put(playerNickname,clientToBeNotified);
                server.addPlayerToRecord(playerNickname, Server.connectionType.RMI);
                tryToStartLoadedGame();
                server.broadcastMessage("Player " + playerNickname + " rejoined the game!", playerNickname);
                clientsLobby.put(clientToBeNotified, new RmiNickStruct(playerNickname));
                clientsLobby.get(clientToBeNotified).setLastPing(System.currentTimeMillis());
                checkForDisconnectionsV2(clientsLobby.get(clientToBeNotified));
                return true;
            }
        }
        System.out.println("nickname not correct");
        return false;
    }

    /**
     * this method is used to load the game present on the json file if present
     * @return true if the game has been loaded from file, false if the json file is not present or if there were any error in reading
     * the json file
     * @throws RemoteException
     */
    public boolean loadGameProgressFromFile() throws RemoteException{
        return controller.loadGameProgress();
    }

    /**
     * tihs method is used to get the personal goal card map
     * @return the personal goal card map
     * @throws RemoteException
     */
    @Override
    public Map<Integer, PersonalGoalCard> getPGCmap() throws RemoteException {
        return controller.getPGCmap();
    }

    /**
     * this method is called by the client to get a matrix of tile representing his personal goal card
     * @param playerNickname name of the player who is requesting his personal goal card
     * @return a matrix of tiles representing his personal goal card
     * @throws RemoteException
     */
    @Override
    public Tile[][] getMyPersonalGoal(String playerNickname) throws RemoteException {
        return controller.getMyPersonalCard(playerNickname);
    }

    /**
     * this method is called by the clients to get the text description of the two common goal cards
     * @return a string containing the description of the two common goal cards
     * @throws RemoteException
     */
    @Override
    public String getCommonGoalCardDescription() throws RemoteException {
        return controller.getCommonGoalCard1Description() + " Token: "+
                controller.getAvailableScoringTokens(controller.getCommonGoalCards().get(0)).
                get(controller.getAvailableScoringTokens(controller.getCommonGoalCards().get(0)).size() -1 ).getPoints()
                + "\n" +controller.getCommonGoalCard2Description() + " Token: " +
                controller.getAvailableScoringTokens(controller.getCommonGoalCards().get(0)).
                get(controller.getAvailableScoringTokens(controller.getCommonGoalCards().get(1)).size() -1 ).getPoints();
    }


    public void flushServer(){

    }

    /**
     * This method is used to notify a rmi client that his turn is starting
     * @param playerNickname name of the player to notify
     */
    public void notifyStartOfTurn(String playerNickname){
        try{
            ClientNotificationInterfaceRMI clientToBeNotified;
            clientToBeNotified = clients.get(playerNickname);
            if(clientToBeNotified!=null){
                clientToBeNotified.startTurn();
                System.out.println("Notified "+ playerNickname +" to start the turn");
            }

        } catch (RemoteException e) {
            System.out.println("Cannot notify client: "+playerNickname);
        }

    }

    /**
     * this method is used to notify all the rmi clients that the game has started
     */
    public void notifyStartOfGame() {
        try{
            for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
                if(client.getValue()!=null){
                    client.getValue().startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                    client.getValue().announceCommonGoals(controller.getCommonGoalCard1Description()+"\n"+controller.getCommonGoalCard2Description());
                    if(client.getKey().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()))
                        notifyStartOfTurn(client.getKey());
                }

            }
        } catch (RemoteException e) {
            System.out.println("Cannot notify client");
        }

    }

    /**
     *  Notifies all clients that game has ended.
     */
    public void notifyEndOfGame() {
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            try {
                if(client.getValue()!=null)
                    client.getValue().gameIsOver(controller.getLeaderboard());
            } catch (RemoteException e) {
                //System.out.println("Cannot notify controller");
            }
        }
    }

    /**
     * Notifies all clients that game has ended by disconnection.
     * @param disconnection true if game has ended by disconnection
     */
    public void notifyEndOfGame(List<Player> finalLead, boolean disconnection) {
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            try {
                if (!disconnection) {
                    if (client.getValue() != null)
                        client.getValue().gameIsOver(controller.getLeaderboard());
                }else{
                    if (client.getValue() != null) {
                        System.out.println("Leaderboard");
                        finalLead.stream().forEach(System.out::println);
                        client.getValue().gameIsOver(finalLead);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Cannot notify controller");
            }
        }
    }

    /**
     * this method is used to set the controller
     * @param controller
     */
    public void setController(Controller controller){
        this.controller = controller;
    }

    /**
     * this method is used to get the name of the player who is currently playing its turn
     * @return
     */
    public String getIsPlaying(){
        return controller.getNameOfPlayerWhoIsCurrentlyPlaying();
    }

    /**
     * This method is called by the client to quit the game and after that the game end
     * @param playerNickname name of the player who sent the quit command
     * @throws IOException
     */
    public void quitGame(String playerNickname) throws RemoteException {
        System.out.println("Server received quit command");
        clients.remove(playerNickname);
        try {
            controller.endGame();
        } catch (IOException e) {
            System.out.println("problem in deleting the json file");
        }
    }

    /**
     * this method is used to send a chat message
     * @param senderName name of the person sending the message
     * @param text text of the message
     * @param receiverName name of the receiver of the message
     * @param pm if true that means that it is a private message
     * @throws RemoteException
     */
    @Override
    public void chatMessage(String senderName, String text, String receiverName, Boolean pm) throws RemoteException {
        if(clients.get(receiverName)==null){            //receiverName not in RMI clients map?
            server.chatMessage(senderName, text, receiverName, pm);
        }
        else{
            clients.get(receiverName).receiveMessage(text, senderName, pm);
        }
    }

    public void addClient(String playerName){
        clients.put(playerName, null);
    }

    /**
     * this method is used to get the personal goal card of the requesting player
     * @param playerNickname name of the requesting player
     * @return the personal goal card of the player if the game has started, null otherwise
     * @throws RemoteException
     */
    public PersonalGoalCard getPGC(String playerNickname) throws RemoteException{
        if(controller.hasGameStarted()){
            return controller.getPGC(playerNickname);
        }
        else return null;
    }

    /**
     * this method is used to get the leaderboard of the current game
     * @return the leaderboard if the game has started, null otherwise
     * @throws RemoteException
     */
    @Override
    public List<Player> getLeaderboard() throws RemoteException {
        if(controller.hasGameStarted())
            return controller.getLeaderboard();
        else
            return null;
    }

    /**
     * this method is used to get the common goal cards of the actual game
     * @return the list of common goals if the game has already started, null otherwise
     * @throws RemoteException
     */
    @Override
    public List<CommonGoalCard> getCommonGoalCards() throws RemoteException {
        if(controller.hasGameStarted()){
            return controller.getCommonGoalCards();
        }
        else
            return null;
    }

    /**
     * this method is used to periodically check if the client rmi has disconnected
     * @param client the rmi client on which the periodic disconnection check is done
     */
    public void checkForDisconnectionsV2(RmiNickStruct client) {
        new Thread(() -> {
            try{
                String nickname = client.getNickname();
                while (true) {
                    //if game hasn't been created yet
                    if (!controller.hasGameBeenCreated()) {
                        if (System.currentTimeMillis() - client.getLastPing() > 3000){
                            clientsLobby.entrySet().removeIf(entry -> entry.getValue().equals(client));
                            server.notifyLobbyDisconnection(nickname);          //notifies server that a disconnection has occurred
                            clients.remove(client.getNickname());
                            return;
                        }
                    }
                    //else if player is in the game
                    else {
                        if(!clientsLobby.containsValue(client)){
                            return;
                        }
                        //if game hasn't started yet
                        if (controller.getGamePlayerListNickname().contains(client.getNickname()) && !controller.hasGameStarted()) {
                            if (System.currentTimeMillis() - client.getLastPing() > 5000) {
                                controller.removePlayer(client.getNickname());
                                clientsLobby.entrySet().removeIf(entry -> entry.getValue().equals(client));
                                server.notifyLobbyDisconnection(nickname);
                                server.clientsMap.remove(nickname);
                                clients.remove(client.getNickname());
                                return;
                            }
                        }
                        //if player is in the game and the game started
                        else {
                            if (System.currentTimeMillis() - client.getLastPing() > 5000){
                                for (int i = 0; i < server.clientsLobby.size(); i++) {
                                    if(server.clientsLobby.get(i).getNickname().equals(client.getNickname())){
                                        server.clientsLobby.get(i).setDisconnected(true);
                                        clients.put(client.getNickname(), null);
                                        if(controller.getNameOfPlayerWhoIsCurrentlyPlaying().equals(client.getNickname())){
                                            endOfTurn(client.getNickname());
                                        }
                                        server.notifyLobbyDisconnection(nickname);
                                        return;
                                    }
                                }

                            }
                        }
                    }
                    Thread.sleep(500);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
    /**
     * this method is called by clientRMI's function heartbeat() and it's used to set the last ping of the client that called it in
     * clientsLobby map
     * @param nickname is the nickname of the clients that needs his lastPing updated
     */
    public synchronized void setLastPing(String nickname){
        for (Map.Entry<ClientNotificationInterfaceRMI, RmiNickStruct> client : clientsLobby.entrySet()){
            if(client.getValue()!=null)
                if(nickname.equals(client.getValue().getNickname()))
                    client.getValue().setLastPing(System.currentTimeMillis());
        }
    }

    /**
     * Notifies every rmi client that a disconnection has occurred
     * @throws RemoteException
     */
    public void notifyLobbyDisconnectionRMI() throws RemoteException{
        for (Map.Entry<ClientNotificationInterfaceRMI, RmiNickStruct> client : clientsLobby.entrySet()){
            if(server.clientsLobby.stream().noneMatch(cis -> cis.getNickname().equals(client.getValue().getNickname()) && cis.isDisconnected())) {
                System.out.println("notifico " + client.getValue().getNickname());
                client.getKey().notifyOfDisconnection();
            }
        }
    }

    /**
     * this method is used to get the first player present in the lobby
     * @return the nickname of the first player in the lobby
     * @throws RemoteException
     */
    public String getFirstClientInLobby() throws RemoteException{
        return server.clientsLobby.get(0).getNickname();
    }

    /**
     * this method is called to get the scoring tokens of the requesting players
     * @param player name of the requesting player
     * @return the list of all the scoring tokens of this player
     * @throws RemoteException
     */
    @Override
    public List<ScoringToken> getMyTokens(String player) throws RemoteException {
        return controller.getScoringToken(player);
    }

    /**
     * this method is called to get the list of tokens of the requested common goal
     * @param commonGoalCard the requested common goal card
     * @return the list of scoring token of the commonGoalCard
     * @throws RemoteException
     */
    @Override
    public List<ScoringToken> getCgcTokens(CommonGoalCard commonGoalCard) throws RemoteException {
        return controller.getAvailableScoringTokens(commonGoalCard);
    }

    /**
     * this method is used by the clients to ping the server
     * @throws RemoteException
     */
    @Override
    public void ping() throws RemoteException{
    }

    /**
     * this method is used to get the list of the players in the game
     * @return the list of players present in the game
     * @throws RemoteException
     */
    @Override
    public List<Player> getPlayers() throws RemoteException {
        return controller.getPlayers();
    }

    /**
     * This method is used to check if the player with the selected nickname has the end game token
     * @param nickname nickname of the player
     * @return true if the player has the end game token, false otherwise
     * @throws RemoteException
     */
    @Override
    public boolean haveIEndGameToken(String nickname) throws RemoteException {
        return controller.hasEndgameToken(nickname);
    }

    /**
     * Thie method is used to check if the player with this nickname is connected with rmi or not
     * @param nickname nickname of the player
     * @return true if the player is connected with rmi, false otherwise
     */
    public boolean isHeARmiPlayer(String nickname){
        return clients.containsKey(nickname);
    }

    /**
     * this method is used to check if the player is disconnected or not
     * @param nickname the name of the player
     * @return true if the player is disconnected, false otherwise
     */
    public boolean isHeDisconnected(String nickname){
        return clients.get(nickname)==null;
    }

    /**
     * this method is used to check after the game has been loaded from json file if all the players are reconnected, in that case it
     * sends the notifications that allow the players to start playing
     * @throws RemoteException
     */
    public void tryToStartLoadedGame() throws RemoteException {
        System.out.println("trying to start the game...");
        if(server.numberOfPlayersLeft() == controller.getNumOfPlayers()){
            System.out.println("all players ok");
            if(server.loadedFromFile){
                System.out.println("loaded check ok...");
                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    if(client.getValue()!=null && (server.clientsLobby.stream().noneMatch(cis -> cis.getNickname().equals(client.getKey()) && cis.isDisconnected()))){
                        System.out.println("start game sent");
                        client.getValue().startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                        if(client.getKey().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())){
                            client.getValue().startTurn();
                            System.out.println("start turn sent to "+client.getKey());
                        }

                    }
                }
                server.loadedFromFile = false;
                server.notifyServer();
            }

        }
    }

    /**
     * this method is used to send a chat message to all the players connected
     * @param message body of the message
     * @param sender name of the person sending the message
     */
    public void broadcastMessage(String message, String sender){
        try{
            for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
                if(client.getValue()!=null) {
                    if(!client.getKey().equals(sender))
                        client.getValue().broadcastedMessage("Message from Server: " + message);
                }
            }
        } catch (RemoteException e) {
            System.out.println("Cannot send message");
        }
    }

    /**
     * this method returns the nickname of the player who holds the first player seat token
     * @return the name of the player who is holding the first player seat, null if for some reason nobody has the first player seat
     */
    public String getFirstPlayerToPlay(){
        Optional<Player> first = controller.getPlayers().stream().filter(Player::getFirstPlayerSeat).findFirst();
        return first.map(Player::getNickname).orElse(null);
    }

    /**
     * This method is used to update the view of the common goal tokens present on each common goal cards
     */
    public void updateCommonGoalTokens(){
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            if(client.getValue()!=null){
                try {
                    client.getValue().updateCommonGoalTokens();
                } catch (RemoteException e) {
                    System.out.println("cannot contact rmi clients");
                }
            }
        }
    }

}
