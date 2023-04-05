package Server;

import main.java.it.polimi.ingsw.Model.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ControllerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{

    private Game game;
    private List<ClientNotificationRecord> clients;



    protected ControllerRMI() throws RemoteException {super();
        game = null;
        clients = new ArrayList<ClientNotificationRecord>();
    }

    @Override
    public int joinGame(String nickname,int port) throws java.rmi.RemoteException {
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if(game==null) {
            System.out.println("There is no game to join, create a new one "+nickname);
            clientToBeNotified.problemInJoiningGame("There is no game to join");
            return -1;
        }
        if(game.hasGameStarted()){
            System.out.println("The game has alredy started, "+nickname+" can't join");
            clientToBeNotified.problemInJoiningGame("Sorry, the game has already started");
            return -2;
        }
        if(game.addPlayer(nickname)){
            System.out.println(nickname+" joined the game");
            clientToBeNotified.gameJoinedCorrectlyNotification();
            clients.add(new ClientNotificationRecord(nickname,clientToBeNotified));
            for(ClientNotificationRecord clientNotificationRecord:clients){
                clientNotificationRecord.client.someoneJoinedTheGame(nickname);
            }
            if(game.hasGameStarted()){
                for(ClientNotificationRecord clientNotificationRecord:clients){
                    clientNotificationRecord.client.statingTheGame(game.isPlaying.getNickname());
                }
            }
            return 0;
        }
        else{
            System.out.println("Nickname alredy used");
            clientToBeNotified.problemInJoiningGame("The nickname you chose is already being used");
            return -3;
        }

    }

    @Override
    public boolean createNewGame(String nickname, int numOfPlayers,int port) throws java.rmi.RemoteException{
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if(game==null){
            game = new Game(numOfPlayers);
            game.addPlayer(nickname);
            clientToBeNotified.gameCreatedCorrectly();
            clients.add(new ClientNotificationRecord(nickname,clientToBeNotified));
            System.out.println("Created new game by "+nickname);
            return true;
        }
        clientToBeNotified.cannotCreateNewGame("There is already a game to join");
        System.out.println("There is already a game to join");
        return false;


    }


    @Override
    public boolean drawTilesFromBoard(String playerNickname, int gameId) throws java.rmi.RemoteException{
        return false;
    }

    public static void main(String[] args){
        try{
            ControllerRMI server = new ControllerRMI();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MyShelfie",server);
        }catch(Exception e){
            e.printStackTrace();

        }

    }
}
