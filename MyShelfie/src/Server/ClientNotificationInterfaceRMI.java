package Server;

import main.java.it.polimi.ingsw.Model.Player;

import java.rmi.RemoteException;
import java.util.List;

public interface ClientNotificationInterfaceRMI extends java.rmi.Remote{
    public void gameJoinedCorrectlyNotification() throws RemoteException;
    public void problemInJoiningGame(String problem) throws RemoteException;

    public void gameCreatedCorrectly() throws RemoteException;

    public void cannotCreateNewGame(String problem) throws RemoteException;

    public void someoneJoinedTheGame(String nickname) throws RemoteException;

    public void startingTheGame(String startingPlayer) throws RemoteException;

    public void someoneHasCompletedACommonGoal(String playerNickname) throws RemoteException;

    public void aTurnHasEnded(String currentPlayerNickname,String nextPlayerNickname) throws RemoteException;

    //void myTurnIsStarting() throws RemoteException;

    public void gameIsOver(List<Player> leaderboard) throws RemoteException;

    public void moveIsNotValid() throws RemoteException;

    public void announceCommonGoals(String commonGoals) throws RemoteException;

   // public void myTurnIsOver() throws RemoteException;

    public void runOutOfTime() throws RemoteException;



}
