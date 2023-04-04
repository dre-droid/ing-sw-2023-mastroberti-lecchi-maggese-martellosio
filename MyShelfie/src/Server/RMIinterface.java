package Server;

public interface RMIinterface extends java.rmi.Remote{

    public boolean joinGame(String nickname) throws java.rmi.RemoteException;

    public boolean createNewGame(String nickname, int numOfPlayers) throws java.rmi.RemoteException;
    public boolean drawTilesFromBoard(String playerNickname,int gameId) throws java.rmi.RemoteException;

}
