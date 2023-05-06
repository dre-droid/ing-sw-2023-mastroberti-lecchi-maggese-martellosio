package Server.RMI;

import main.java.it.polimi.ingsw.Model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIinterface extends java.rmi.Remote{

    public int joinGame(String nickname,int port) throws java.rmi.RemoteException;

    public boolean createNewGame(String nickname, int numOfPlayers,int port) throws java.rmi.RemoteException;
    public List<Tile> drawTilesFromBoard(String playerNickname, int x, int y, int amount, Board.Direction direction) throws java.rmi.RemoteException;

    public boolean hasGameStarted() throws RemoteException;

    public Tile[][] getMyShelf(String playerNickname) throws RemoteException;

    public boolean isMyTurn(String playerNickname) throws RemoteException;

    public TilePlacingSpot[][] getBoard() throws RemoteException;

    public boolean insertTilesInShelf(String playerNickname,List<Tile> tiles,int column) throws RemoteException;

    //public void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException;

    //public void endOfTurn(String playerNickname) throws RemoteException;

    public int getPoints(String playerNickname) throws RemoteException;

    public boolean isGameOver() throws RemoteException;

    public boolean reconnect(String playerNickname, int port) throws RemoteException;

    public Tile[][] getMyPersonalGoal(String playerNickname) throws  RemoteException;

    public String getCommonGoalCardDescription() throws RemoteException;

    public void quitGame(String playerNickname) throws RemoteException;

    public void chatMessage(String senderName, String text, String receiverName) throws RemoteException;
}
