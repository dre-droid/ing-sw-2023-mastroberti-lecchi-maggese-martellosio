package Server;

import Server.RMI.ServerRMI;
import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class Server {
    public enum connectionType{
        RMI, Socket
    }

    public ServerRMI serverRMI;
    public ServerSock serverSock;
    private Map<String, connectionType> clients;
    private Controller controller;

    public void run(){
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
            clients = new HashMap<>();
            controller = new Controller(this);
            serverSock.setController(controller);
            controller.setServerSock(serverSock);
            //serverRMI.setController(controller);

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
                    if (clients.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).equals(connectionType.Socket)) {
                        System.out.println(controller.getNameOfPlayerWhoIsCurrentlyPlaying() + " starting the turn");
                        controller.playTurn();
                    }
                    serverSock.hasDisconnectionOccurred();
                }
                if (serverSock.hasDisconnectionOccurred())
                    gameEnd(serverSock.getNameOfDisconnection());
                else gameEnd(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }

            //game end handling
            System.out.println("Game has ended. Accepting players for new game...");
            serverSock.flushServer();
            serverRMI.flushServer();    //needs implementation
        }while(true);
    }
    public static void main(String[] args) throws InvalidMoveException, InterruptedException {
        Server server = new Server();
        server.run();
    }

    public void addPlayerToRecord(String nickname, connectionType conn){
        clients.put(nickname,conn);
    }

    /**
     * handles player quitting game: results in game ending, all players should be notified of the event
     * @param nick - the nick of the player who quit or disconnected
     * @throws IOException
     */
    private void gameEnd(String nick) throws IOException {
        //rmi notify
        serverSock.notifyGameEnd(nick);
    }

}
