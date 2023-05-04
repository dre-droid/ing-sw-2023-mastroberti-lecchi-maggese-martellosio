package Server;

import Server.RMI.ClientNotificationInterfaceRMI;
import Server.RMI.ServerRMI;
import Server.Socket.ServerSock;
import Server.Socket.socketNickStruct;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Server {
    public enum connectionType {
        RMI, Socket
    }

    public boolean disconnection;
    public ServerRMI serverRMI;
    public ServerSock serverSock;
    private Map<String, connectionType> clientsMap;
    private Controller controller;

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
            controller = new Controller(this);
            serverSock.setController(controller);
            controller.setServerSock(serverSock);
            serverRMI.setController(controller);

            //waits that all players connect
            while (!controller.hasGameStarted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            serverSock.notifyGameStart(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            while (!controller.hasTheGameEnded()) {
                Thread.sleep(500);
                if (clientsMap.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).equals(connectionType.Socket)) {
                    controller.playTurn();
                }
            }

            //game end handling
            System.out.println("Game has ended. Accepting players for new game...");
            serverSock.flushServer();
            serverRMI.flushServer();    //needs testing
        } while (true);
    }

    public static void main(String[] args) throws InvalidMoveException, InterruptedException {
        Server server = new Server();
        server.run();
    }

    public void addPlayerToRecord(String nickname, connectionType conn) {
        clientsMap.put(nickname, conn);
    }

    public void chatMessage(String sender, String text, String receiver){
        //broadcast message
        if(receiver.equals("all")){
            for(Map.Entry<String, connectionType> client: clientsMap.entrySet()){
                if(!client.getKey().equals(sender))
                    if(client.getValue()==connectionType.RMI){
                        try{
                            serverRMI.chatMessage(sender, text, client.getKey());
                        } catch (RemoteException e) {
                            System.out.println("Cannot send message to "+receiver);
                        }
                    }else{
                        try {
                            serverSock.sendChatMessageToClient(sender, text, client.getKey());
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
                    serverRMI.chatMessage(sender, text, receiver);
                } catch (RemoteException e) {
                    System.out.println("Cannot send message to "+receiver);
                }
            }
            //send message to socket client
            else{
                try {
                    serverSock.sendChatMessageToClient(sender, text, receiver);
                } catch (IOException e) {
                    System.out.println("Cannot send message to"+receiver);
                }
            }
        }
    }

}
