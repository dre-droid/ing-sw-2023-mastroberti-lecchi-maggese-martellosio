package Server;

import java.rmi.RemoteException;

public interface ClientNotificationInterfaceRMI extends java.rmi.Remote{
    public void gameJoinedCorrectlyNotification() throws RemoteException;
    public void problemInJoiningGame(String problem) throws RemoteException;

    public void gameCreatedCorrectly() throws RemoteException;

    public void cannotCreateNewGame(String problem) throws RemoteException;

    public void someoneJoinedTheGame(String nickname) throws RemoteException;

    public void statingTheGame(String startingPlayer) throws RemoteException;

}
