package Server;

import Server.RMI.ServerRMI;
import Server.Socket.ServerSock;
import Server.Socket.socketNickStruct;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;
import java.io.IOException;
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

    public void run() {
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
            //plays turns
            try {
                while (!controller.hasTheGameEnded()) {
                    Thread.sleep(500);
                    if (clientsMap.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).equals(connectionType.Socket)) {
                        controller.playTurn();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            //game end handling
            System.out.println("Game has ended. Accepting players for new game...");
            serverSock.flushServer();
            serverRMI.flushServer();    //needs implementation
        } while (true);
    }

    public static void main(String[] args) throws InvalidMoveException, InterruptedException {
        Server server = new Server();
        server.run();
    }

    public void addPlayerToRecord(String nickname, connectionType conn) {
        clientsMap.put(nickname, conn);
    }

}