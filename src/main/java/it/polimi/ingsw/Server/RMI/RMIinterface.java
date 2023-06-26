package main.java.it.polimi.ingsw.Server.RMI;

import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface RMIinterface extends java.rmi.Remote{
    public int joinLobby(String nickname, int port, String ip) throws RemoteException;
    public int joinGame(String nickname,int port, String ip) throws java.rmi.RemoteException;

    public boolean createNewGame(String nickname, int numOfPlayers,int port, String ip) throws java.rmi.RemoteException;
    public List<Tile> drawTilesFromBoard(String playerNickname, int x, int y, int amount, Board.Direction direction) throws java.rmi.RemoteException;
    public boolean isGameBeingCreated() throws RemoteException;
    public boolean firstInLobby (String nickname) throws RemoteException;
    public void gameIsCreated() throws RemoteException;

    public boolean hasGameStarted() throws RemoteException;

    public Tile[][] getMyShelf(String playerNickname) throws RemoteException;

    public boolean isMyTurn(String playerNickname) throws RemoteException;

    public TilePlacingSpot[][] getBoard() throws RemoteException;

    public boolean insertTilesInShelf(String playerNickname,List<Tile> tiles,int column) throws RemoteException;

    //public void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException;

    //public void endOfTurn(String playerNickname) throws RemoteException;

    public int getPoints(String playerNickname) throws RemoteException;

    public boolean isGameOver() throws RemoteException;

    public boolean reconnect(String playerNickname, int port, String ip) throws RemoteException;

    public Tile[][] getMyPersonalGoal(String playerNickname) throws  RemoteException;

    public String getCommonGoalCardDescription() throws RemoteException;

    public void quitGame(String playerNickname) throws RemoteException;

    public void chatMessage(String senderName, String text, String receiverName, Boolean pm) throws RemoteException;

    public boolean loadGameProgressFromFile() throws RemoteException;

    public Map<Integer, PersonalGoalCard> getPGCmap() throws RemoteException;

    public PersonalGoalCard getPGC(String playerNickname) throws RemoteException;

    public List<Player> getLeaderboard() throws RemoteException;

    public List<CommonGoalCard> getCommonGoalCards() throws RemoteException;

    public String getIsPlaying() throws RemoteException;
    public void setLastPing(String nickname)throws RemoteException;
    public String getFirstClientInLobby() throws RemoteException;

    public List<ScoringToken> getMyTokens(String player) throws RemoteException;

    public List<ScoringToken> getCgcTokens(CommonGoalCard commonGoalCard) throws RemoteException;

    public void ping() throws RemoteException;

    public List<Player> getPlayers() throws RemoteException;

    public boolean haveIEndGameToken(String nickname) throws RemoteException;

    public String getFirstPlayerToPlay() throws RemoteException;
}
