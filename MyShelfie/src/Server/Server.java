package Server;

import Server.RMI.ServerRMI;
import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.InvalidMoveException;

import javax.sound.midi.SysexMessage;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class Server {
    //static ServerRMI serverRMI;
    public enum connectionType{
        RMI, Socket
    }


    public ServerRMI serverRMI;
    public ServerSock serverSock;
    private Map<String, connectionType> clients;
    private Controller controller;

    public void run(){
        clients = new HashMap<>();
        controller = new Controller(this);
        try{
            serverRMI = new ServerRMI(controller,this);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MyShelfie",serverRMI);
        }catch(Exception e){
            e.printStackTrace();
        }
        serverSock = new ServerSock(controller, this);
        serverSock.runServer();
        controller.setServerSock(serverSock);
        //serverSock.setController(controller);
        clients.forEach((k,v)->System.out.println(k+", "+v));
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        while(!controller.hasGameStarted()){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        clients.forEach((k,v)->System.out.println(k+", "+v));
        try{
            while (!controller.hasTheGameEnded()) {
                Thread.sleep(500);
                clients.forEach((k,v)->System.out.println(k+", "+v));
                if (clients.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).equals(connectionType.Socket)) {
                    System.out.println(controller.getNameOfPlayerWhoIsCurrentlyPlaying()+" starting the turn");
                    controller.playTurn();
                    if (controller.hasTheGameEnded()) {
                    }//game ending stuff
                }
            }
        }catch (InvalidMoveException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws InvalidMoveException, InterruptedException {

        Server server = new Server();
        server.run();
        /*ServerSock serverSocket = new ServerSock();
        Controller controller = new Controller(serverSocket);
        serverSocket.setController(controller);

        serverSocket.runServer();
        //run ServerRMI
        try {
            ServerRMI server = new ServerRMI();
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
        }*/
    }

    public void addPlayerToRecord(String nickname, connectionType conn){
        clients.put(nickname,conn);
    }

}
