package Server.RMI;

import Server.Controller;
import Server.Server;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ServerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{

    //Timer timerDraw;
    //Timer timerInsert;

    Server server;
    Controller controller;

    private final int drawDelay= 60000;
    private final int insertDelay = 70000;
    //private List<ClientNotificationRecord> clients;

    private Map<String, ClientNotificationInterfaceRMI> clients;



    public ServerRMI(Controller controller, Server server) throws RemoteException {super();
        clients = new HashMap();
        //timerDraw = new Timer();
        //timerInsert = new Timer();
        this.controller = controller;
        this.server = server;

    }

    /**
     * This method is called to join the game, if the controller added the player correctly it alerts the client and add it to the
     * clients list, if the controller cannot add the player to the game (oucome=-1,-2,-3) it sends a message to the client with the
     * corresponding error
     * @param nickname name of the player that wants to join the game
     * @param port used to connect to the rmi client
     * @return 0 if the player has been added to the game, -1, -2, -3 if there has been an error in the joining of the game
     * @throws java.rmi.RemoteException
     */
    @Override
    public int joinGame(String nickname,int port) throws java.rmi.RemoteException {
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        int outcome = controller.joinGame(nickname);
        switch (outcome){
            case 0: {
                clientToBeNotified.gameJoinedCorrectlyNotification();
                clients.put(nickname, clientToBeNotified);
                //clients.add(new ClientNotificationRecord(nickname, clientToBeNotified));
                /*for (ClientNotificationRecord clientNotificationRecord : clients) {
                    clientNotificationRecord.client.someoneJoinedTheGame(nickname);
                }*/
                server.addPlayerToRecord(nickname, Server.connectionType.RMI);
                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    client.getValue().someoneJoinedTheGame(nickname);
                }
                if (controller.hasGameStarted()) {
                    /*startTimer(timerDraw, drawDelay);*/
                    notifyStartOfGame();
                    /*for (ClientNotificationRecord clientNotificationRecord : clients) {
                        clientNotificationRecord.client.startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                        clientNotificationRecord.client.announceCommonGoals(commonGoals);
                    }*/
                }
            }break;
            case -1: {
                clientToBeNotified.problemInJoiningGame("There is no game to join");
            }break;
            case -2: {
                clientToBeNotified.problemInJoiningGame("Sorry, the game has already started");
            }break;
            case -3: {
                clientToBeNotified.problemInJoiningGame("The nickname you chose is already being used");
            }break;
        }
        return outcome;
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
    public boolean createNewGame(String nickname, int numOfPlayers,int port) throws java.rmi.RemoteException{
        if(numOfPlayers<0 || numOfPlayers>4){
            System.out.println("Wrong parameters");
            return false;
        }

        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if(controller.createNewGame(nickname, numOfPlayers)){
            clientToBeNotified.gameCreatedCorrectly();
            //clients.add(new ClientNotificationRecord(nickname,clientToBeNotified));
            clients.put(nickname, clientToBeNotified);
            server.addPlayerToRecord(nickname, Server.connectionType.RMI);
            System.out.println("Created new game by "+nickname);
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
        List<Tile> drawnTiles = controller.drawFromBoard(playerNickname, x, y, amount, direction);
        if(drawnTiles==null){
            //clients.stream().filter(client->client.nickname.equals(playerNickname)).toList().get(0).client.moveIsNotValid();
            clients.get(playerNickname).moveIsNotValid();
            return null;
        }
        /*timerDraw.cancel();
        timerInsert = new Timer();
        startTimer(timerInsert,insertDelay);*/
        return drawnTiles;
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

    @Override
    public TilePlacingSpot[][] getBoard() throws RemoteException {
        return controller.getBoard();
    }

    @Override
    public boolean insertTilesInShelf(String playernickName, List<Tile> tiles, int column) throws RemoteException{
        if(controller.insertTilesInShelf(playernickName, tiles, column)){
            //timerInsert.cancel();
            System.out.println("ServerRMI-->tiles correctly inserted");
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


    private void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())) {
            if (controller.checkIfCommonGoalN1IsFulfilled(playerNickname)) {

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard1Description());
                }

                /*for (ClientNotificationRecord c : clients) {
                    c.client.someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard1Description());
                }*/
            }
            if (controller.checkIfCommonGoalN2IsFulfilled(playerNickname)){

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard2Description());
                }

                /*for (ClientNotificationRecord c : clients) {
                    c.client.someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard2Description());
                }*/
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
            checkIfCommonGoalsHaveBeenFulfilled(playerNickname);

            for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                client.getValue().aTurnHasEnded(playerNickname, controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            }

            //ClientNotificationInterfaceRMI clientToBeNotified = clients.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            /*if(clientToBeNotified!=null)
                clientToBeNotified.startTurn();*/
            server.notifySocketOfTurnEnd(playerNickname);
            controller.endOfTurn(playerNickname);

            if(controller.hasTheGameEnded()){
                notifyEndOfGame();
            }
            //saveGameProgress();

        }
    }

    /*private void startTimer(Timer timer, int delay){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    //clients.stream().filter(client -> client.nickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())).toList().get(0).client.runOutOfTime();
                    clients.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).runOutOfTime();
                    endOfTurn(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        },delay);
    }*/




    @Override
    public int getPoints(String playerNickname) throws RemoteException {
        return controller.getPoints(playerNickname);
    }

    @Override
    public boolean isGameOver() throws RemoteException {
        return controller.hasTheGameEnded();
    }

    @Override
    public boolean reconnect(String playerNickname, int port)  throws RemoteException{
        //we check if there is a player with this name in the game
        if(clients.containsKey(playerNickname)){
            //we check if the client is already connected with a ping, if the client is not connected we proceed to the reconnection
            System.out.println("Correct nickname");
            //we update the list of the clients with the new client if we can connect to it
            if(clients.get(playerNickname)==null){
                ClientNotificationInterfaceRMI clientToBeNotified;
                try{
                    Registry registry = LocateRegistry.getRegistry(port);
                    clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
                } catch (NotBoundException | java.rmi.RemoteException  ex) {
                    System.out.println("Cannot connect to the new client");
                    return false;
                }
                clients.put(playerNickname, clientToBeNotified);
                server.addPlayerToRecord(playerNickname, Server.connectionType.RMI);
                if(server.isEveryoneConnected()){
                    for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                        client.getValue().startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                        if(client.getKey().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()))
                            client.getValue().startTurn();
                    }
                }

                System.out.println("Connected to the new client");
                return true;
            }
            return false;

            //}
        }
        else{
            System.out.println("nickname not correct");
            return false;
        }
    }

    public boolean loadGameProgressFromFile() throws RemoteException{
        return controller.loadGameProgress();
    }

    @Override
    public Map<Integer, PersonalGoalCard> getPGCmap() throws RemoteException {
        return controller.getPGCmap();
    }

    @Override
    public Tile[][] getMyPersonalGoal(String playerNickname) throws RemoteException {
        return controller.getMyPersonalCard(playerNickname);
    }

    @Override
    public String getCommonGoalCardDescription() throws RemoteException {
        return controller.getCommonGoalCard1Description()+"\n"+controller.getCommonGoalCard2Description();
    }


    public void flushServer(){

    }

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

    public void notifyStartOfGame() {
        try{
            for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
                client.getValue().startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                client.getValue().announceCommonGoals(controller.getCommonGoalCard1Description()+"\n"+controller.getCommonGoalCard2Description());
                if(client.getKey().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()))
                    notifyStartOfTurn(client.getKey());
            }
        } catch (RemoteException e) {
            System.out.println("Cannot notify client");
        }

    }

    public void notifyEndOfGame() {
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            try {
                List<String> leaderboard = controller.getLeaderboard().stream().map(player->player.getNickname()+" pts: "+player.getScore()).toList();
                client.getValue().gameIsOver(leaderboard);
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Cannot notify controller");
            }
        }
    }

    public void setController(Controller controller){
        this.controller = controller;
    }

    public String getIsPlaying(){
        return controller.getNameOfPlayerWhoIsCurrentlyPlaying();
    }

    public void quitGame(String playerNickname) throws RemoteException{
        System.out.println("Server received quit command");
        controller.endGame();
    }

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

    public PersonalGoalCard getPGC(String playerNickname) throws RemoteException{
        return controller.getPGC(playerNickname);
    }

    @Override
    public List<Player> getLeaderboard() throws RemoteException {
        if(controller.hasGameStarted())
            return controller.getLeaderboard();
        else
            return null;
    }

    @Override
    public List<CommonGoalCard> getCommonGoalCards() throws RemoteException {
        if(controller.hasGameStarted()){
            return controller.getCommonGoalCards();
        }
        else
            return null;
    }


}
