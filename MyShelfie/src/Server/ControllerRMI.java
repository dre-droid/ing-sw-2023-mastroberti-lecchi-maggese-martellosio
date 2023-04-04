package Server;

import main.java.it.polimi.ingsw.Model.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ControllerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{

    private Game game;

    protected ControllerRMI() throws RemoteException {super();
        game = null;
    }

    @Override
    public boolean joinGame(String nickname) throws java.rmi.RemoteException {
        if(game==null) {
            return false;
        }
        game.addPlayer(nickname);
        System.out.println(nickname+" joined the game");
        return true;
    }

    @Override
    public boolean createNewGame(String nickname, int numOfPlayers) throws java.rmi.RemoteException{
        game = new Game(numOfPlayers);
        game.addPlayer(nickname);
        System.out.println("Created new game by "+nickname);
        return true;
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
