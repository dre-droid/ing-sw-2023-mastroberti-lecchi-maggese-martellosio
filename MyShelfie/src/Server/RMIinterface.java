package Server;

public interface RMIinterface extends java.rmi.Remote{

    public int joinGame(String nickname,int port) throws java.rmi.RemoteException;

    public boolean createNewGame(String nickname, int numOfPlayers,int port) throws java.rmi.RemoteException;
    public boolean drawTilesFromBoard(String playerNickname,int gameId) throws java.rmi.RemoteException;

}
