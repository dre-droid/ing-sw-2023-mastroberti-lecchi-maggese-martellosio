package Server;

import Server.RMI.ServerRMI;
import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;
import main.java.it.polimi.ingsw.Model.Player;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Server {
    private static final int TIMEOUT_THRESH = 60000; //in millis

    public enum connectionType {
        RMI, Socket;
    }
    public ServerRMI serverRMI;
    public ServerSock serverSock;
    public Map<String, connectionType> clientsMap;
    private Controller controller;
    public ArrayList<ClientInfoStruct> clientsLobby;
    public boolean onlyOnePlayer = false;
    public final Object clientsLobbyLock = new Object();

    public boolean loadedFromFile;

    public void run() throws InterruptedException, IOException {
        //run Socket and RMI servers
        serverSock = new ServerSock(controller, this);
        serverSock.runServer();
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            serverRMI = new ServerRMI(controller, this);
            registry.rebind("MyShelfie", serverRMI);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            while (!controller.hasTheGameEnded() && !onlyOnePlayer) {
                for (int i = 0; i < clientsLobby.size(); i++) {
                    if (clientsLobby.get(i).getNickname().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()) && clientsLobby.get(i).isDisconnected()) {
                        controller.endOfTurn(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                    }
                }
                Thread.sleep(500);
                //notifies clients socket whose turn is it
                notifySocketOfTurnEnd();
                //calls playTurn() of the player who is currently playing according to controller
                if (clientsMap.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).equals(connectionType.Socket)) {
                    controller.playTurn();
                    // notify clients that turn has ended
                    notifySocketOfTurnEnd();
                }
            }

            //game end handling
            System.out.println("Game has ended. Accepting players for new game...");
            serverRMI.notifyEndOfGame();
            controller.deleteProgress();
            //wait(100000);
            //Thread.sleep(100000);
            //serverSock.flushServer();
            //serverRMI.flushServer();    //needs testing

        }while (!controller.hasTheGameEnded());
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
                    onlyOnePlayer = true;

                    // if no one connects in TIMEOUT_THRESH declare the remaining player the winner, end the game
                    synchronized (o) {
                        if (numberOfPlayersLeft() == 1) o.wait(TIMEOUT_THRESH);
                        if (numberOfPlayersLeft() == 1) {
                            controller.disconnectionEndGame();
                        }
                    }
                    onlyOnePlayer = false;
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

    public void chatMessage(String sender, String text, String receiver, Boolean pm){
        //broadcast message
        if(receiver.equals("all")){
            for(Map.Entry<String, connectionType> client: clientsMap.entrySet()){
                if(!client.getKey().equals(sender))
                    if(client.getValue()==connectionType.RMI){
                        try{
                            serverRMI.chatMessage(sender, text, client.getKey(), pm);
                        } catch (RemoteException e) {
                            System.out.println("Cannot send message to "+receiver);
                        }
                    }else{
                        try {
                            serverSock.sendChatMessageToClient(sender, text, client.getKey(), pm);
                        } catch (IOException e) {
                            System.out.println("Cannot send message to"+receiver);
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
                    System.out.println("Cannot send message to "+receiver);
                }
            }
            //send message to socket client
            else{
                try {
                    serverSock.sendChatMessageToClient(sender, text, receiver, pm);
                } catch (IOException e) {
                    System.out.println("Cannot send message to"+receiver);
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
     * Notifies all clients that a player has disconnected. Notifies all threads waiting on this (onePlayerLeftTimout() waits for it).
     * @param nickOfDisconnectedPlayer the nickname of the player that disconnected
     * @throws RemoteException
     */
    public synchronized void notifyLobbyDisconnection(String nickOfDisconnectedPlayer) throws RemoteException{
        serverRMI.notifyLobbyDisconnectionRMI();
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
        System.out.println("a player disconnected from the lobby");
        notify();
    }

    public void notifyGameHasBeenCreated() throws RemoteException{
        serverRMI.gameIsCreated();
        serverSock.notifyGameHasBeenCreatedSocket();
        notifyServer();
    }

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
                throw new RuntimeException(e);
            }



        }).start();
    }

    public void broadcastMessage(String message, String sender){
        serverSock.broadcastMessage("[MESSAGE FROM SERVER] " + message, sender);
        serverRMI.broadcastMessage(message, sender);
    }

    public int numberOfPlayersLeft(){
        int numOfPlayerLeft = 0;
        for (int i = 0; i < clientsLobby.size(); i++) {
            if (controller.getGamePlayerListNickname().contains(clientsLobby.get(i).getNickname())){
                if(!clientsLobby.get(i).isDisconnected())
                    numOfPlayerLeft++;
            }
        }
//        System.out.println("num of players connected = "+numOfPlayerLeft);
        return numOfPlayerLeft;
    }
    public void notifyServer(){
        synchronized (this){
            notifyAll();
        }
    }

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
     * To use only fot testing purposes
     * @param serverRMI
     */
    public void setServerRMI(ServerRMI serverRMI){
        this.serverRMI=serverRMI;
    }

    /**
     * To use only fot testing purposes
     * @param serverSock
     */
    public void setServerSock(ServerSock serverSock){
        this.serverSock=serverSock;
    }


}
