package Server;

import main.java.it.polimi.ingsw.Model.Player;

import java.rmi.RemoteException;
import java.util.List;

public class ClientNotificationRMI extends java.rmi.server.UnicastRemoteObject implements ClientNotificationInterfaceRMI{
    protected ClientNotificationRMI() throws RemoteException {
        super();
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
    }

    @Override
    public void someoneHasCompletedACommonGoal(String playerNickname) throws RemoteException {
        System.out.println(playerNickname+" has completed a common goal, congratulations!");
    }

    @Override
    public void aTurnHasEnded(String currentPlayerNickname, String nextPlayerNickname) throws RemoteException {
        System.out.println(currentPlayerNickname+"'s turn has ended, now it's "+nextPlayerNickname+"'s turn!");
    }

    @Override
    public void gameIsOver(List<Player> leaderboard) throws RemoteException {
        System.out.println("The game has ended, here is the leaderboard: ");
        for(int i=0;i<leaderboard.size();i++){
            System.out.println((i+1)+") "+leaderboard.get(i).getScore());
        }
    }

    @Override
    public void moveIsNotValid() throws RemoteException {
        System.out.println("Invalid move, try something different!");
    }

}
