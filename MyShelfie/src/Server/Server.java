package Server;

import Server.Socket.ServerSock;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    static ServerSock serverSock;
    static ServerRMI serverRMI;
    public static void main(String[] args) {
        Controller controller = new Controller();
        serverSock = new ServerSock(controller);
        serverSock.runServer();
        //run ServerRMI
        //fai giocare turno al primo giocatore
        //if (controller.hasGameStarted()) controller.playTurn();
        //while (!controller.hasTheGameEnded())
            //controller.getNextPlayer().playTurn;

        //controlla fine partita


    }
}
