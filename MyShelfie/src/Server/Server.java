package Server;

import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Timer;

public class Server {
    static ServerRMI serverRMI;
    public static void main(String[] args) throws InvalidMoveException, InterruptedException {
        ServerSock serverSocket = new ServerSock();
        Controller controller = new Controller(serverSocket);
        serverSocket.setController(controller);

        serverSocket.runServer();
        //run ServerRMI

        //fai giocare turno al primo giocatore
        while (true){
            Thread.sleep(1000);
            if (controller.hasGameStarted()) {
                controller.playTurn();
                if (controller.hasTheGameEnded()) {}//game ending stuff}
            }
        }
    }
}
