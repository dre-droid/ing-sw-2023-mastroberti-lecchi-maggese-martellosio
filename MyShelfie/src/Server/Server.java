package Server;

import Server.RMI.ServerRMI;
import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    static ServerRMI serverRMI;
    public static void main(String[] args) throws InvalidMoveException, InterruptedException {
        ServerSock serverSocket = new ServerSock();
        Controller controller = new Controller(serverSocket);
        serverSocket.setController(controller);

        serverSocket.runServer();
        //run ServerRMI
        try {
            ServerRMI server = new ServerRMI(controller);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MyShelfie", server);
        } catch (Exception e) {
            e.printStackTrace();

            //fai giocare turno al primo giocatore
            while (true) {
                Thread.sleep(500);
                if (controller.hasGameStarted()) {
                    controller.playTurn();
                    if (controller.hasTheGameEnded()) {
                    }//game ending stuff
                }
            }
        }
    }
}
