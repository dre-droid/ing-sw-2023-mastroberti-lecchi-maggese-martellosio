package Server;

import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    static ServerSock serverSock;
    static ServerRMI serverRMI;
    public static void main(String[] args) throws InvalidMoveException {
        Controller controller = new Controller(serverSock);
        ServerSock serverSocket = new ServerSock(controller);
        serverSocket.runServer();
        //run ServerRMI

        //fai giocare turno al primo giocatore
        while (true){
            if (controller.hasGameStarted()) {
                System.out.println("Game started");
                controller.playTurn();
                if (controller.hasTheGameEnded()) {}//game ending stuff}
            }
        }
    }
}
