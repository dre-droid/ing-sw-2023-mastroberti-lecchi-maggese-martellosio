package Server.RMI;

import Server.Controller;
import Server.Server;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import Server.ClientInfoStruct;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

//TODO client will receive chat messages from socket clients while a game hasn't started yet, but won't send any until the game started, the previous messages will appear
public class ServerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{

    //Timer timerDraw;
    //Timer timerInsert;

    Server server;
    Controller controller;
    private final long DISCONNECTION_TIME = 7000;  //disconnection threshold: 5s
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
        checkForDisconnections();

    }

    /**
     *This method is called by ClientRMI to join a map Lobby, used to alert every ClientRMI waiting for a game to be created
     * @author Diego Lecchi
     * @param nickname name of the player that wants to join the lobby
     * @param port used to connect to the rmi client
     * @return 0 if the player has been added to the map
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
        return 0;
    }
    //TODO gestire se più giocatori joinano rispetto a numero partita
    /**
     * This method is called to join the game, if the controller added the player correctly it alerts the client and add it to the
     * clients list, if the controller cannot add the player to the game (outcome=-1,-2,-3) it sends a message to the client with the
     * corresponding error
     * @param nickname name of the player that wants to join the game
     * @param port used to connect to the rmi client
     * @return 0 if the player has been added to the game, -1, -2, -3 if there has been an error in the joining of the game
     * @throws java.rmi.RemoteException
     */
    @Override
    public int joinGame(String nickname,int port,String ip) throws java.rmi.RemoteException {
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(ip, port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        int outcome = controller.joinGame(nickname);
        switch (outcome){
            case 0: {
                clientToBeNotified.gameJoinedCorrectlyNotification();
                clients.put(nickname, clientToBeNotified);
                server.addPlayerToRecord(nickname, Server.connectionType.RMI);
                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    if(client.getValue()!=null)
                        client.getValue().someoneJoinedTheGame(nickname);
                }
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
            /*
            case -3: {
                clientToBeNotified.problemInJoiningGame("The nickname you chose is already being used");
            }break;

             */
        }
        clientToBeNotified.joinGameOutcome(outcome);
        return outcome;
    }
    public boolean isGameBeingCreated() throws RemoteException{
        return controller.isGameBeingCreated;
    }
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

        /*new Thread(() -> {
            while(!controller.hasGameStarted()) {
                try {
                    if (clientsLobby.size() > 0) {
                        Set<ClientNotificationInterfaceRMI> set = clientsLobby.keySet();
                        for (ClientNotificationInterfaceRMI c : set) {
                            c.gameHasBeenCreated();

                        }
                    }
                } catch (RemoteException e) {
                    System.out.println("Cannot notify client");
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

         */
        //in this version every client enters at the same time so a thread that iterates is not necessary
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
        List<Tile> drawnTiles = controller.drawFromBoard(playerNickname, x, y, amount, direction);
        if(drawnTiles==null){
            //clients.stream().filter(client->client.nickname.equals(playerNickname)).toList().get(0).client.moveIsNotValid();
            System.out.println("move is not valid");
            clients.get(playerNickname).moveIsNotValid();
            return null;
        }
        /*timerDraw.cancel();
        timerInsert = new Timer();
        startTimer(timerInsert,insertDelay);*/
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            if(client.getValue()!=null){
                client.getValue().updateBoard(controller.getTilePlacingSpot());
            }
        }
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
        return controller.getTilePlacingSpot();
    }

    @Override
    public boolean insertTilesInShelf(String playernickName, List<Tile> tiles, int column) throws RemoteException{
        if(controller.insertTilesInShelf(playernickName, tiles, column)){
            //timerInsert.cancel();
            System.out.println("ServerRMI-->tiles correctly inserted");
            for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                if(!client.getKey().equals(playernickName)){
                    if(client.getValue()!=null)
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


    private void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())) {
            if (controller.checkIfCommonGoalN1IsFulfilled(playerNickname)) {

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    if(client.getValue()!=null)
                        client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCards().get(0));
                }

                /*for (ClientNotificationRecord c : clients) {
                    c.client.someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard1Description());
                }*/
            }
            if (controller.checkIfCommonGoalN2IsFulfilled(playerNickname)){

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    if(client.getValue()!=null)
                        client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCards().get(1));
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



            //ClientNotificationInterfaceRMI clientToBeNotified = clients.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            /*if(clientToBeNotified!=null)
                clientToBeNotified.startTurn();*/
            server.notifySocketOfTurnEnd(playerNickname);
            controller.endOfTurn(playerNickname);
            for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                if(client.getValue()!=null)
                    client.getValue().aTurnHasEnded(playerNickname, controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            }
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
                        if(client.getValue()!=null){
                            client.getValue().startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                            if(client.getKey().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()))
                                client.getValue().startTurn();
                        }
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

    public void notifyEndOfGame() {
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            try {
                List<String> leaderboard = controller.getLeaderboard().stream().map(player->player.getNickname()+" pts: "+player.getScore()).toList();
                if(client.getValue()!=null)
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

    public void quitGame(String playerNickname) throws IOException {
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
     * This method is used to check if rmi clients are still connected, if not it removes the one that disconnected from both clientsInLobby
     * list in server and from clientsLobby map and alerts the server of the disconnection by calling server.notifyLobbyDisconnection()
     * @author Diego Lecchi
     */
    private void checkForDisconnections(){
        new Thread(() -> {
            try {
                while(Objects.isNull(controller)) Thread.sleep(1000);
                //while (!controller.hasGameStarted()) Thread.sleep(3000);
                while(!controller.hasGameBeenCreated()) Thread.sleep(1000);
                while (!controller.hasTheGameEnded()) {
                    Iterator<Map.Entry<ClientNotificationInterfaceRMI, RmiNickStruct>> iterator = clientsLobby.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<ClientNotificationInterfaceRMI, RmiNickStruct> entry = iterator.next();
                        ClientNotificationInterfaceRMI client = entry.getKey();
                        RmiNickStruct nickStruct = entry.getValue();

                        if (System.currentTimeMillis() - nickStruct.getLastPing() > DISCONNECTION_TIME) {
                            String nickOfDisconnectedPlayer = nickStruct.getNickname();

                            //server.clientsLobby.removeIf(clientInfoStruct -> nickOfDisconnectedPlayer.equals(clientInfoStruct.getNickname()));
                            System.out.println(nickOfDisconnectedPlayer +" disconnected");
                            iterator.remove();
                            clients.put(nickOfDisconnectedPlayer, null);//we set null the value in clients
                            if(controller.isMyTurn(nickOfDisconnectedPlayer)){
                                endOfTurn(nickOfDisconnectedPlayer);
                            }
                            server.notifyLobbyDisconnection(nickOfDisconnectedPlayer);
                        }
                    }
                    //if only one connected player remains than the game must end in 30 seconds if nobody reconnects
                    if(getNumOfConnectedPlayers()==1 && controller.hasGameBeenCreated()){
                        //end the game (after a timer)
                        if(hasGameStarted()){
                            System.out.println("only one player left, the game must end dun dun dunnnn");
                            controller.endGame();
                        }

                        //notifyEndOfGame();
                    }
                    Thread.sleep(300);

                }
                //System.out.println("has the game ended: " + controller.hasTheGameEnded());


            }catch(Exception e){
                e.printStackTrace();
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
            client.getKey().notifyOfDisconnection();
        }
    }

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

    @Override
    public void ping() throws RemoteException{
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
     * This method is used to get the number of player that are not disconnected
     * @return the number of connected players
     */
    public int getNumOfConnectedPlayers(){
        int counter=0;
        for (Map.Entry<ClientNotificationInterfaceRMI, RmiNickStruct> client : clientsLobby.entrySet()){
            if(client.getValue()!=null){
                counter++;
            }
        }
        System.out.println("Num of connected players = "+counter);
        return counter;
    }
}

//TODO unire client e clientsLobby
//TODO aggiungere la possibilità di connettersi da due computer con rmi