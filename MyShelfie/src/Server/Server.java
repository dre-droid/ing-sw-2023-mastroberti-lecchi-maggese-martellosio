package Server;

import Server.Socket.ServerSock;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) {
        Controller controller = new Controller();
        ServerSock serverSocket = new ServerSock(controller);
        serverSocket.runServer();
        //run ServerRMI
        //fai giocare turno al primo giocatore
        //if (controller.hasGameStarted()) controller.getFirstPlayer().playTurn();
        //while (!controller.hasTheGameEnded())
            //controller.getNextPlayer().playTurn;

        //controlla fine partita


    }
}
