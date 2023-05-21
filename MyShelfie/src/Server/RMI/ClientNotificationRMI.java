package Server.RMI;

import main.java.it.polimi.ingsw.Model.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClientNotificationRMI extends java.rmi.server.UnicastRemoteObject implements ClientNotificationInterfaceRMI{

    ClientRMI clientRMI;

    public ClientNotificationRMI(ClientRMI clientRMI) throws RemoteException {
        super();
        this.clientRMI = clientRMI;
    }



    @Override
    public void gameJoinedCorrectlyNotification() throws RemoteException{
        System.out.println("Joined the game");
    }

    @Override
    public void problemInJoiningGame(String problem) throws RemoteException{
        System.out.println("Cannot join the game because: "+problem);
    }

    @Override
    public void gameCreatedCorrectly() throws RemoteException{
        System.out.println("Game created successfully");
    }

    @Override
    public void cannotCreateNewGame(String problem) throws RemoteException {
        System.out.println("Cannot create a new game because: "+problem);
    }

    @Override
    public void someoneJoinedTheGame(String nickname) throws RemoteException {
        System.out.println(nickname + " has joined the game!");
    }

    @Override
    public void startingTheGame(String startingPlayer) throws RemoteException {
        System.out.println("The game has started, "+startingPlayer+" will be the first to play!!");
        clientRMI.setGameStartFlag(true);
        synchronized (this){
            notify();
        }
    }

    @Override
    public void someoneHasCompletedACommonGoal(String playerNickname, String commongoal) throws RemoteException {
        System.out.println(playerNickname+" has completed a common goal, congratulations!");
        System.out.println("The common goal completed is:");
        System.out.println(commongoal);
    }

    @Override
    public void aTurnHasEnded(String currentPlayerNickname, String nextPlayerNickname) throws RemoteException {
        System.out.println(currentPlayerNickname+"'s turn has ended, now it's "+nextPlayerNickname+"'s turn!");
    }



    @Override
    public void gameIsOver(List<String> leaderboard) throws RemoteException {
        System.out.println("The game has ended, here is the leaderboard: ");
        clientRMI.setEndGameFlag(true);
        for(int i=0;i<leaderboard.size();i++){
            System.out.println((i+1)+") "+leaderboard.get(i));
        }
    }

    @Override
    public void moveIsNotValid() throws RemoteException {
        System.out.println("Invalid move, try something different!");
    }

    @Override
    public void announceCommonGoals(String commonGoals) throws RemoteException {
        System.out.println("The common goals for the game are: \n"+commonGoals);
    }

    @Override
    public void runOutOfTime() throws RemoteException {
        System.out.println("Sorry, you run out of time and lost the turn, type anything to continue!");
    }

    @Override
    public void ping() throws RemoteException {
        System.out.println("Server checking if client is alive");
    }

    @Override
    public void startTurn() throws RemoteException {
        clientRMI.setMyTurnFlag(true);
        System.out.println("Your turn has started, press enter to play your turn");
    }

    @Override
    public void receiveMessage(String text, String sender, Boolean pm) throws RemoteException {
        if(pm)
            System.out.println("[MESSAGE FROM "+sender+" TO YOU]: "+text);
        else
            System.out.println("[MESSAGE FROM "+sender+"]: "+text);
    }

    @Override
    public void invalidCommandSent() throws RemoteException {
        System.out.println("The command you sent has invalid parameters");
    }


}
