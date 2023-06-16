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
    public enum connectionType {
        RMI, Socket
    }

    public boolean disconnection;
    public ServerRMI serverRMI;
    public ServerSock serverSock;
    public Map<String, connectionType> clientsMap;
    public ArrayList<String> connectedClients = new ArrayList<>();      //TODO need to add in RMI
    private Controller controller;
    //public ArrayList<String> clientsInLobby = new ArrayList<>();
    public ArrayList<ClientInfoStruct> clientsLobby;
    public final Object clientsLobbyLock = new Object();

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

        //server iterates do-while for every new game
        do {
            //resets clients and creates new controller (hence new game)
            clientsMap = new HashMap<>();
            clientsLobby = new ArrayList<>();
            controller = new Controller(this);
            serverSock.setController(controller);
            controller.setServerSock(serverSock);
            serverRMI.setController(controller);
            joinGame();

            // waits that all players connect and game starts
            synchronized (controller) {
                while (!controller.hasGameStarted() && isEveryoneConnected()) {
                    controller.wait();
                }
            }
            Thread.sleep(1000);
            // game starts
            serverSock.notifyGameStart(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            while (!controller.hasTheGameEnded()) {
                Thread.sleep(100);

                //if only one player remains the game is put on hold. If nobody rejoins the game ends
                //TODO if the game ends this way it should show the remaining player as winner, NOT THE LEADERBOARD
                if(numberOfPlayersLeft() == 1){
                    synchronized (this){
                        wait(60000);
                    }
                    if(numberOfPlayersLeft() == 1){
                        controller.endGame();
                    }
                }
                for (int i = 0; i < clientsLobby.size(); i++) {
                    if(clientsLobby.get(i).getNickname().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()) && clientsLobby.get(i).isDisconnected()){
                        controller.endOfTurn(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                    }
                }

                if (clientsMap.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).equals(connectionType.Socket)) {
                    controller.playTurn();
                    notifySocketOfTurnEnd(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                }
            }

            //game end handling
            System.out.println("Game has ended. Accepting players for new game...");
            serverSock.flushServer();
            serverRMI.flushServer();    //needs testing
        } while (true);
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
    public void notifySocketOfTurnEnd(String isPlaying){
        serverSock.broadcastMessage("[CURRENTPLAYER]" + isPlaying);
    }

    public boolean isEveryoneConnected(){
        for(Map.Entry<String, connectionType> client: clientsMap.entrySet()){
            if(client.getValue()==null)
                return false;
        }
        return true;
    }
    public void notifyLobbyDisconnection(String nickOfDisconnectedPlayer) throws RemoteException{
        serverRMI.notifyLobbyDisconnectionRMI();
        serverSock.notifyLobbyDisconnectionSocket();
        if(!controller.hasGameStarted()) {
            synchronized (clientsLobbyLock) {
                clientsLobby.removeIf(clientInfoStruct -> nickOfDisconnectedPlayer.equals(clientInfoStruct.getNickname()));
            }
        }
        notifyServer();
        System.out.println("a player disconnected from the lobby");
    }
    public void notifyGameHasBeenCreated() throws RemoteException{
        serverRMI.gameIsCreated();
        serverSock.notifyGameHasBeenCreatedSocket();
        notifyServer();
    }

    public void addPlayerToConnectedClients(String nick){
        connectedClients.add(nick);
    }

    public String getConnectedPlayer(int index){
        return connectedClients.get(index);
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
                        serverSock.joinGame(clientsLobby.get(0).getSocket(), clientsLobby.get(0).getNickname(),
                                clientsLobby.get(0).getOut(), clientsLobby.get(0).getReader());
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
                                        serverSock.joinGame(client.getSocket(), client.getNickname(), client.getOut(), client.getReader());
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
    public void broadcastMessage(String message){
        //TODO fare una funzione broadcast in rmi e socket che scrive tramite chat un errore o notifica (cosi funziona anche in GUI), una sorta di [Message from Server]
    }
    public int numberOfPlayersLeft(){
        int numOfPlayerLeft = 0;
        for (int i = 0; i < clientsLobby.size(); i++) {
            if (controller.getGamePlayerListNickname().contains(clientsLobby.get(i).getNickname())){
                if(!clientsLobby.get(i).isDisconnected())
                    numOfPlayerLeft++;
            }
        }
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

}
