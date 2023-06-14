package Server;

import Server.RMI.ServerRMI;
import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;
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
    public Object clientsLobbyLock = new Object();

    public void run() throws InterruptedException {
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

            // game starts
            serverSock.notifyGameStart(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            while (!controller.hasTheGameEnded()) {
                Thread.sleep(100);
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
        synchronized (clientsLobbyLock){
            clientsLobby.removeIf(clientInfoStruct -> nickOfDisconnectedPlayer.equals(clientInfoStruct.getNickname()));
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
            try {       //TODO fare che si entra uno alla volta dopo che si e' scelto il numero di giocatori
                int oldClientsLobbySize = 0;
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
                        //System.out.println("chiamata la join per "+ clientsLobby.get(0).getNickname());
                    } else {                                            //else client is socket
                        serverSock.joinGame(clientsLobby.get(0).getSocket(), clientsLobby.get(0).getNickname(),
                                clientsLobby.get(0).getOut(), clientsLobby.get(0).getReader());
                        System.out.println("chiamata la join per "+ clientsLobby.get(0).getNickname());
                    }
                }

                synchronized (this) {
                    while (clientsLobby.size() < controller.game.getNumOfPlayers()) {
                        //System.out.println("entrato in clientsLobby.size() < controller.game.getNumOfPlayers()");
                        this.wait();
                    }
                }
                synchronized(clientsLobbyLock) {
                    for (int i = 1; i < clientsLobby.size(); i++) {
                        if (clientsLobby.get(i).getRmiPort() != 0) {        //if client is rmi
                            serverRMI.joinGame(clientsLobby.get(i).getNickname(), clientsLobby.get(i).getRmiPort(), clientsLobby.get(i).getRmiIp());
                            System.out.println("chiamata la join per " + clientsLobby.get(i).getNickname());
                        } else {                                            //else client is socket
                            serverSock.joinGame(clientsLobby.get(i).getSocket(), clientsLobby.get(i).getNickname(),
                                    clientsLobby.get(i).getOut(), clientsLobby.get(i).getReader());
                            System.out.println("chiamata la join per "+ clientsLobby.get(i).getNickname());
                        }
                    }
                }

                //join for the rest of the players that will try to connect even tho a game is already being played
                oldClientsLobbySize = clientsLobby.size();
                while(controller.hasGameBeenCreated()){
                    synchronized (this){
                        while(oldClientsLobbySize == clientsLobby.size())
                            this.wait();
                    }
                    for (int i = oldClientsLobbySize; i < clientsLobby.size(); i++) {
                        if (clientsLobby.get(i).getRmiPort() != 0) {        //if client is rmi
                            serverRMI.joinGame(clientsLobby.get(i).getNickname(), clientsLobby.get(i).getRmiPort(), clientsLobby.get(i).getRmiIp());
                            //System.out.println("chiamata la join per "+ clientsLobby.get(i).getNickname());
                        } else {                                            //else client is socket
                            serverSock.joinGame(clientsLobby.get(i).getSocket(), clientsLobby.get(i).getNickname(),
                                    clientsLobby.get(i).getOut(), clientsLobby.get(i).getReader());
                            System.out.println("chiamata la join per "+ clientsLobby.get(i).getNickname());
                        }
                    }
                    oldClientsLobbySize = clientsLobby.size();
                }


                /*
                synchronized (this) {
                    while (true) {
                        synchronized (clientsLobbyLock) {
                            Iterator<ClientInfoStruct> iterator = clientsLobby.iterator();
                            ClientInfoStruct client = iterator.next();
                            while (client.getNickname().equals(clientsLobby.get(0).getNickname()))  //skip the first element
                                iterator.next();
                            while (iterator.hasNext()) {
                                client = iterator.next();
                                if (client.getRmiPort() != 0) { // if client is RMI
                                    serverRMI.joinGame(client.getNickname(), client.getRmiPort());
                                    System.out.println("chiamata la join per " + client.getNickname());
                                } else { // else client is socket
                                    serverSock.joinGame(client.getSocket(), client.getNickname(), client.getOut(), client.getReader());
                                    System.out.println("chiamata la join per " + client.getNickname());
                                }
                            }
                        }
                        wait();
                    }
                }
                */
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }



        }).start();
    }
    public void notifyServer(){
        synchronized (this){
            notifyAll();
        }
    }

    public static void main(String[] args) throws InvalidMoveException, InterruptedException {
        Server server = new Server();
        server.run();
    }

}
