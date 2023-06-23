package Server.RMI;

import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Model.Player;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.TilePlacingSpot;

import java.rmi.RemoteException;
import java.util.List;

public interface ClientNotificationInterfaceRMI extends java.rmi.Remote{
    public void gameJoinedCorrectlyNotification() throws RemoteException;
    public void problemInJoiningGame(String problem) throws RemoteException;

    public void gameCreatedCorrectly() throws RemoteException;
    public void joinGameOutcome(int outcome) throws RemoteException;

    public void cannotCreateNewGame(String problem) throws RemoteException;

    public void someoneJoinedTheGame(String nickname) throws RemoteException;
    public void gameHasBeenCreated() throws RemoteException;

    public void startingTheGame(String startingPlayer) throws RemoteException;

    public void someoneHasCompletedACommonGoal(String playerNickname, CommonGoalCard cgc) throws RemoteException;

    public void aTurnHasEnded(String currentPlayerNickname,String nextPlayerNickname) throws RemoteException;

    //void myTurnIsStarting() throws RemoteException;

    public void gameIsOver(List<Player> leaderboard) throws RemoteException;

    public void moveIsNotValid() throws RemoteException;

    public void announceCommonGoals(String commonGoals) throws RemoteException;

   // public void myTurnIsOver() throws RemoteException;

    public void runOutOfTime() throws RemoteException;

    public void ping() throws RemoteException;

    public void startTurn() throws RemoteException;

    public void receiveMessage(String text, String sender, Boolean pm) throws RemoteException;
    public void sendChatMessage(String message) throws RemoteException;

    public void invalidCommandSent() throws RemoteException;

    public void updateBoard(TilePlacingSpot[][] boardView) throws RemoteException;

    public void updateOppShelf(String nickname, Tile[][] grid) throws RemoteException;

    public void nickNameAlreadyInUse() throws RemoteException;
    public void notifyOfDisconnection() throws RemoteException;
    public void broadcastedMessage(String message) throws RemoteException;

    public void updateCommonGoalTokens() throws RemoteException;


}
