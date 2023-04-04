package Server;

import main.java.it.polimi.ingsw.Model.*;

import java.rmi.RemoteException;

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
        return true;
    }

    @Override
    public boolean createNewGame(String nickname, int numOfPlayers) throws java.rmi.RemoteException{
        game = new Game(numOfPlayers);
        game.addPlayer(nickname);
        return true;
    }


    @Override
    public boolean drawTilesFromBoard(String playerNickname, int gameId) throws java.rmi.RemoteException{
        return false;
    }
}
