package main.java.it.polimi.ingsw.Server;

import main.java.it.polimi.ingsw.Server.RMI.ServerRMI;
import main.java.it.polimi.ingsw.Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    private static final int TIMEOUT_THRESH = 60000; //in millis

    public final Object onePlayerLeftLock = new Object();

    public enum connectionType {
        RMI, Socket;
    }
    public ServerRMI serverRMI;
    public ServerSock serverSock;
    public Map<String, connectionType> clientsMap;
    public Controller controller;
    public ArrayList<ClientInfoStruct> clientsLobby;
    public final Object clientsLobbyLock = new Object();

    public boolean loadedFromFile;

    /**
     * Runs the server, handling the game flow and player connections. Starts the Socket and RMI servers, then loads game
     * progress from a JSON file if available, or proceeds with the regular join process. Waits for all players to connect
     * and the game to start. Enters the game loop, where players take turns until the game ends.
     * Handles disconnections and skips turns of disconnected players, handles the end of the game and performs cleanup
     * operations.
     * @throws InterruptedException If the execution is interrupted while waiting.
     * @throws IOException If an I/O error occurs.
     */
    public void run() throws InterruptedException, IOException {
        //run Socket and RMI servers


        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            serverRMI = new ServerRMI(controller, this);
            registry.rebind("MyShelfie", serverRMI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        do{
            serverSock = new ServerSock(controller, this);
            serverSock.runServer();
            do {

                //initiates clients and creates new controller
                clientsMap = new HashMap<>();
                clientsLobby = new ArrayList<>();
                controller = new Controller(this);
                serverSock.setController(controller);
                controller.setServerSock(serverSock);
                serverRMI.setController(controller);

                // loading from json GameProgress file
                if (controller.loadGameProgress()){     //true if GameProgress.json is present and if so loadGameProgress will load it
                    loadedFromFile = true;
                    for (int i = 0; i < controller.getGamePlayerListNickname().size(); i++) {       //for every nickname now in game
                        clientsLobby.add(new ClientInfoStruct(controller.getGamePlayerListNickname().get(i)));
                        clientsLobby.get(i).setDisconnected(true);  //add a ClientInfoStruct object in clientsLobby with the same name
                    }                                               //and set disconnected as true to trigger a rejoin
                    synchronized (this) {
                        while (clientsLobby.stream().anyMatch(client -> client.isDisconnected())){
                            this.wait();        //wait for all the player in the saved game to join to resume the game
                        }
                    }
                }
                else {      //otherwise GameProgress.json is not present so the usual join is called
                    joinGame();
                }

                // waits that all players connect and game starts
                synchronized (controller) {
                    while (!controller.hasGameStarted() && isEveryoneConnected()) {
                        controller.wait();
                    }
                }
                Thread.sleep(1000);

                // game starts
                serverSock.notifyGameStart(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                onePlayerLeftTimeout();
                while (!controller.hasTheGameEnded()) {
                    //System.out.println("-------hola--------");
                    // if only one player left, wait
                    synchronized (onePlayerLeftLock) {
                        if (numberOfPlayersLeft() == 1) {
                            // notify last client that he's the only player left
                            clientsLobby.stream()
                                    .filter(c -> !c.isDisconnected() && c.getSocket() != null)
                                    .forEach(c -> serverSock.sendMessage("[INFO] Everyone disconnected! After a timeout passed an no rejoins, you'll have won.", c.getNickname())); //TODO show to GUI
                            //System.out.println("server print 3");
                            onePlayerLeftLock.wait();
                        }
                    }

                    /*if(clientsLobby.stream().anyMatch(ClientInfoStruct::isDisconnected)){
                        controller.endGame();
                    }*/

                    // skip turn of disconnected player if current
                    if (controller.game != null && !controller.hasTheGameEnded()) {
                        for (int i = 0; i < clientsLobby.size(); i++) {
                            if (clientsLobby.get(i).getNickname().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()) && clientsLobby.get(i).isDisconnected()) {
                                //System.out.println("player "+ controller.getNameOfPlayerWhoIsCurrentlyPlaying()+" is disconnected and should skip turn");
                                controller.endOfTurn(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                                notifySocketOfTurnEnd();
                                serverRMI.notifyEndOfTurn(clientsLobby.get(i).getNickname());
                            }
                        }
                    }


                    //calls playTurn() of the player who is currently playing according to controller
                    if (controller.game != null && !controller.hasTheGameEnded()) {
                        if (clientsMap.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).equals(connectionType.Socket)) {
                            controller.playTurn();
                            notifySocketOfTurnEnd();
                            // RMI update in controller.playTurn()
                        }
                    }
                }
                //game end handling
                //System.out.println("Game has ended.");
                serverSock.notifyGameEnd();
                serverRMI.notifyEndOfGame();
                controller.postEndGame();
                //serverSock.flushServer();
                //serverRMI.flushServer();    //needs testing

            }while (!controller.hasTheGameEnded());
            //server flush
            this.clientsLobby.clear();
            this.clientsMap.clear();
            this.loadedFromFile = false;
            serverRMI.emptyClients();
            serverSock.emptyClients();

            do {
                controller.deleteProgress();
            }while(controller.checkForSavedGameProgress());
            //System.out.println("game ended----------------------");
            System.exit(0);
        }while(true);

    }

    /**
     * Runs a thread that waits until notified by a notifyLobbyDisconnection. When only one player remains, waits for
     * a timeout and ends the game if no one connects.
     */
    private void onePlayerLeftTimeout() {
        new Thread(() -> {
            try {
                while (!controller.hasTheGameEnded()) {
                    final Object o = new Object();
                    // wait that only one player is left
                    synchronized (this) {
                        while (numberOfPlayersLeft() != 1) wait();
                    }
                    // if no one connects in TIMEOUT_THRESH declare the remaining player the winner, end the game
                    synchronized (o) {
                        if (numberOfPlayersLeft() <= 1) o.wait(TIMEOUT_THRESH);
                        if (numberOfPlayersLeft() <= 1) {
                            String finalNick = (clientsLobby.stream().filter(c -> !c.isDisconnected()).toList()).get(0).getNickname();
                            controller.disconnectionEndGame(finalNick);
                            synchronized (onePlayerLeftLock){
                                onePlayerLeftLock.notifyAll();   //notifies server that a player has rejoined
                            }
                        }
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public void addPlayerToRecord(String nickname, connectionType conn) {
        clientsMap.put(nickname, conn);
    }

    /**
     * this method is used to send chat messages to every RMI and Socket client. It's called by serverRMI.chatMessage()
     * and serverSock.sendChatMessageToClient(), if the receiver is "all" then the message passed in parameter "text" will
     * be sent to all the clients but the one who wrote it. If the receiver parameter is a player in the game the message will be
     * sent to that player by calling either serverRMI.chatMessage() or serverSock.sendChatMessageToClient().
     * The Boolean pm (private message) will be used to print either MESSAGE FROM *nick receiver* if pm is false or
     * MESSAGE FROM *nick receiver* TO YOU otherwise. If the receiver parameter is a nickname of a player not present in
     * the game the message will simply be ignored.
     * @param sender nickname of the player who sends the message
     * @param text body of the message
     * @param receiver nickname of the player to whom the chat message is destined (all if the message is for everybody)
     * @param pm Private message, true if the message is destined to one player only
     */
    public void chatMessage(String sender, String text, String receiver, Boolean pm){
        //broadcast message
        if(receiver.equals("all")){
            for(Map.Entry<String, connectionType> client: clientsMap.entrySet()){
                if(!client.getKey().equals(sender))
                    if(client.getValue()==connectionType.RMI){
                        try{
                            serverRMI.chatMessage(sender, text, client.getKey(), pm);
                        } catch (RemoteException e) {
                            //System.out.println("Cannot send message to "+receiver);
                        }
                    }else{
                        try {
                            serverSock.sendChatMessageToClient(sender, text, client.getKey(), pm);
                        } catch (IOException e) {
                            //System.out.println("Cannot send message to"+receiver);
                        }
                    }
            }
        }
        //if the receiver is a player in the game
        if(clientsMap.get(receiver)!=null){
            //send message to rmi player
            if(clientsMap.get(receiver)==connectionType.RMI){
                try{
                    serverRMI.chatMessage(sender, text, receiver, pm);
                } catch (RemoteException e) {
                    //System.out.println("Cannot send message to "+receiver);
                }
            }
            //send message to socket client
            else{
                try {
                    serverSock.sendChatMessageToClient(sender, text, receiver, pm);
                } catch (IOException e) {
                    //System.out.println("Cannot send message to"+receiver);
                }
            }
        }
    }

    /**
     * sends Socket client the name of the current player
     */
    public void notifySocketOfTurnEnd(){
        serverSock.updateGameObjectsAfterTurn();
    }

    public boolean isEveryoneConnected(){
        for(Map.Entry<String, connectionType> client: clientsMap.entrySet()){
            if(client.getValue()==null)
                return false;
        }
        return true;
    }

    /**
     * Notifies all clients that a player has disconnected. Notifies all threads waiting on this (onePlayerLeftTimout() and run() wait for it).
     * @param nickOfDisconnectedPlayer the nickname of the player that disconnected
     */
    public synchronized void  notifyLobbyDisconnection(String nickOfDisconnectedPlayer){
        //System.out.println("notify lobby disconnection has been called lalalalalalalallalaal");
        try {
            serverRMI.notifyLobbyDisconnectionRMI();
        } catch (RemoteException e) {
            //System.out.println("cannot contact rmi server");
        }
        serverSock.notifyLobbyDisconnectionSocket();
        if(!controller.hasGameStarted()) {
            synchronized (clientsLobbyLock) {
                clientsLobby.removeIf(clientInfoStruct -> nickOfDisconnectedPlayer.equals(clientInfoStruct.getNickname()));
            }
        }
        //add when game has already started
        else{
            synchronized (clientsLobbyLock) {
                for(ClientInfoStruct cis: clientsLobby){
                    if(cis.getNickname().equals(nickOfDisconnectedPlayer)){
                        cis.setDisconnected(true);
                    }
                }
            }
        }
        broadcastMessage("Player " + nickOfDisconnectedPlayer + " disconnected", "Server");
        notifyServer();
        //System.out.println("a player disconnected from the lobby");
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Used to notify serverSock, serverRMI and server that a game has been created
     * @throws RemoteException
     */
    public void notifyGameHasBeenCreated() throws RemoteException{
        serverRMI.gameIsCreated();
        serverSock.notifyGameHasBeenCreatedSocket();
        notifyServer();
    }

    /**
     * This method is called by run() whenever a saved game GameProgress.json is not present. It's used to launch the respective
     * method joinGame of ServerRMI and ServerSock in a specific order to avoid more than one player creating a new game
     * at the same time. While the match type isn't chosen yet, the respective joinGame() is called for the first client
     * in clientsLobby, if that client disconnects then it will be removed from clientsLobby and therefore clientsLobby.get(0)
     * is now different from the old clientsLobby.get(0) and so joinGame() will be now executed for the new one.
     * Whenever the match type is selected this method will call joinGame() for the rest of the clients in clientsLobby
     */
    public void joinGame() {
        new Thread (()->{
            try {
                ClientInfoStruct oldFirstClientInList = null;
                while(!controller.hasGameBeenCreated()) {       //while the number of player for the game hasn't been selected yet
                    synchronized (this) {
                        while ((clientsLobby.size() == 0 || clientsLobby.get(0).equals(oldFirstClientInList)) && !controller.hasGameBeenCreated()) {
                            this.wait();
                        }
                    }
                    oldFirstClientInList = clientsLobby.get(0);
                    if (clientsLobby.get(0).getRmiPort() != 0) {        //if client is rmi
                        serverRMI.joinGame(clientsLobby.get(0).getNickname(), clientsLobby.get(0).getRmiPort(), clientsLobby.get(0).getRmiIp());
                    } else {                                            //else client is socket
                        serverSock.joinGame(clientsLobby.get(0).getNickname(),
                                clientsLobby.get(0).getOut());
                    }
                }
                Thread.sleep(1000);

                synchronized (this) {
                    while (true) {
                        synchronized (clientsLobbyLock) {
                            Iterator<ClientInfoStruct> iterator = clientsLobby.iterator();
                            while (iterator.hasNext()) {
                                ClientInfoStruct client = iterator.next();
                                boolean alreadyInGame = false;
                                if(controller.game!=null)
                                    for (int i = 0; i < controller.game.getPlayerList().size(); i++) {      //if the player is already in the game
                                        if(client.getNickname().equals(controller.game.getPlayerList().get(i).getNickname()))
                                            alreadyInGame = true;       //set this bool to true so join won't be executed
                                    }

                                if(!alreadyInGame) {
                                    if (client.getRmiPort() != 0) { // if client is RMI
                                        serverRMI.joinGame(client.getNickname(), client.getRmiPort(), client.getRmiIp());
                                    } else { // else client is socket
                                        serverSock.joinGame(client.getNickname(), client.getOut());
                                    }
                                    notifyGameHasBeenCreated();
                                }
                            }
                        }
                        wait();
                    }
                }

            } catch (InterruptedException | IOException e) {
                //System.out.println("cannot join game");
            }

        }).start();
    }

    /**
     * This method is used to send a message to all RMI and socket clients except the sender of that message
     * @param message - message to broadcast
     * @param sender - nickname of the player that broadcasts the message
     */
    public void broadcastMessage(String message, String sender){
        serverSock.broadcastMessage("[MESSAGE FROM SERVER] " + message, sender);
        serverRMI.broadcastMessage(message, sender);
    }

    /**
     * @return number of players in the game which have the boolean disconnected set to false in the ArrayList of ClientInfoStruct
     * clientsLobby
     */
    public int numberOfPlayersLeft(){
        int numOfPlayerLeft = 0;
        if(controller.game==null){
            return 0;
        }
        for (int i = 0; i < clientsLobby.size(); i++) {
            if (controller.getGamePlayerListNickname().contains(clientsLobby.get(i).getNickname())){
                if(!clientsLobby.get(i).isDisconnected())
                    numOfPlayerLeft++;
            }
        }
        return numOfPlayerLeft;
    }

    /**
     * notifies all the thread waiting on this
     */
    public void notifyServer(){
        synchronized (this){
            notifyAll();
        }
    }

    /**
     * removes a ClientInfoStruct object from the ArrayList clientInfoStruct if an object with this nickname exists
     * @param nickname - nickname of a player that needs to be removed from clientsLobby
     */
    public void removeFromClientsLobby(String nickname){
        synchronized (clientsLobbyLock) {
            clientsLobby.removeIf(clientInfoStruct -> nickname.equals(clientInfoStruct.getNickname()));
        }
    }

    public static void main(String[] args) throws InvalidMoveException, InterruptedException, IOException {
        Server server = new Server();
        server.run();
    }

    /**
     * Used only for testing purposes
     */
    public void setServerRMI(ServerRMI serverRMI){
        this.serverRMI=serverRMI;
    }

    /**
     * Used only for testing purposes
     */
    public void setServerSock(ServerSock serverSock){
        this.serverSock=serverSock;
    }


}
