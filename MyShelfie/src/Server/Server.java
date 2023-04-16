package Server;

import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    static ServerSock serverSock;
    static ServerRMI serverRMI;
    public static void main(String[] args) throws InvalidMoveException {
        Controller controller = new Controller();
        ServerSock serverSocket = new ServerSock(controller);
        serverSocket.runServer();
        //run ServerRMI
        //fai giocare turno al primo giocatore
        if (controller.hasGameStarted()) controller.playTurn();
        while (!controller.hasTheGameEnded())
            controller.playTurn();

        //controlla fine partita


    }
}
