package main.java.it.polimi.ingsw.Server.RMI;

import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.rmi.RemoteException;
import java.util.*;

public class ClientNotificationRMI extends java.rmi.server.UnicastRemoteObject implements ClientNotificationInterfaceRMI{

    ClientRMI clientRMI;

    public ClientNotificationRMI(ClientRMI clientRMI) throws RemoteException {
        super();
        this.clientRMI = clientRMI;
    }


    /**
     * this method is called by the server to notify the player that he joined the game correctly
     * @throws RemoteException
     */
    @Override
    public void gameJoinedCorrectlyNotification() throws RemoteException{
        System.out.println("Joined the game");
    }

    /**
     * this method is called by the server to notify the client that an error occurred in joining the game
     * @param problem text description of the error
     * @throws RemoteException
     */
    @Override
    public void problemInJoiningGame(String problem) throws RemoteException{
        System.out.println("Cannot join the game because: "+problem);
    }

    /**
     * this method is called by the server to notify the client that the game has been correctly created
     * @throws RemoteException
     */
    @Override
    public void gameCreatedCorrectly() throws RemoteException{
        System.out.println("Game created successfully");
    }

    /**
     * this method is called by the server to show the outcome of the joinGame operation to the client
     * @param outcome int value representing the outcome of the operation (see uml sequence diagram for
     *        description of the int value)
     * @throws RemoteException
     */
    public void joinGameOutcome(int outcome) throws RemoteException{
        clientRMI.joinGameOutcome = outcome;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * this method sets the boolean value gameHasBeenCreated in ClientRMI to true and notifies it. Called by ServerRMI method
     * gameIsCreated which in turn is called by Controller when a new game is created
     * @author Diego Lecchi
     * @throws RemoteException
     */
    public void gameHasBeenCreated() throws RemoteException{
        clientRMI.setGameHasBeenCreated(true);
        synchronized (this){
            notifyAll();
        }
    }

    /**
     * this method is called by the server to notify the client that he is trying to use a nickname already
     * used by another player
     * @throws RemoteException
     */
    public void nickNameAlreadyInUse() throws RemoteException{
        System.out.println("This nickname is already in use, try another one");
    }

    /**
     * this method is called by the server to notify the client that the game has not been created correctly
     * @param problem text description of the problem
     * @throws RemoteException
     */
    @Override
    public void cannotCreateNewGame(String problem) throws RemoteException {
        System.out.println("Cannot create a new game because: "+problem);
    }

    /**
     * this method is called by the server to notify the client that a player joined the game
     * @param nickname name of the player that joined the game
     * @throws RemoteException
     */
    @Override
    public void someoneJoinedTheGame(String nickname) throws RemoteException {
        System.out.println(nickname + " has joined the game!");
    }

    /**
     * this method is called by the server to notify the client that the game has started
     * @param startingPlayer name of the player who will play first
     * @throws RemoteException
     */
    @Override
    public void startingTheGame(String startingPlayer) throws RemoteException {
        System.out.println("The game has started, "+startingPlayer+" will be the first to play!!");
        clientRMI.setGameStartFlag(true);
        synchronized (this){
            notify();
        }
    }

    /**
     * this method is called by the server to notify the client that someone has completed a common goal
     * @param playerNickname name of the player that has completed the common goal
     * @param cgc common goal card that has been completed
     * @throws RemoteException
     */
    @Override
    public void someoneHasCompletedACommonGoal(String playerNickname, CommonGoalCard cgc) throws RemoteException {
        System.out.println(playerNickname+" has completed a common goal, congratulations!");
        System.out.println("The common goal completed is:");
        System.out.println(cgc.getDescription());
    }

    /**
     * this method is called by the server to notify the client that a player has ended its turn
     * @param currentPlayerNickname name of the player that ended its turn
     * @param nextPlayerNickname name of the next player that will play its turn
     * @throws RemoteException
     */
    @Override
    public void aTurnHasEnded(String currentPlayerNickname, String nextPlayerNickname) throws RemoteException {
        System.out.println(currentPlayerNickname+"'s turn has ended, now it's "+nextPlayerNickname+"'s turn!");
    }

    /**
     * this method is called by the server to notify the client that the game has ended
     * @param leaderboard leaderboard containg the winner
     * @throws RemoteException
     */
    @Override
    public void gameIsOver(List<Player> leaderboard) throws RemoteException {
        System.out.println("The game has ended, here is the leaderboard: ");
        clientRMI.setEndGameFlag(true);
        for(int i=0;i<leaderboard.size();i++){
            System.out.println((i+1)+") "+leaderboard.get(i).getNickname()+"with "+leaderboard.get(i).getScore());
        }
        System.exit(0);
    }

    /**
     * this method is called by the server to notify the client that the operation he tried to do was invalid
     * @throws RemoteException
     */
    @Override
    public void moveIsNotValid() throws RemoteException {
        System.out.println("Invalid move, try something different!");
    }

    /**
     * this method is called by the server to announce the common goal cards to the client
     * @param commonGoals list of common goal cards
     * @throws RemoteException
     */
    @Override
    public void announceCommonGoals(String commonGoals) throws RemoteException {
        System.out.println("The common goals for the game are: \n"+commonGoals);
    }

    @Override
    public void runOutOfTime() throws RemoteException {
        System.out.println("Sorry, you run out of time and lost the turn, type anything to continue!");
    }

    /**
     * this method is used by the server to check if the client is still connected
     * @throws RemoteException
     */
    @Override
    public void ping() throws RemoteException {
        //System.out.println("Server checking if client is alive");
    }

    /**
     * this method is called by the server to notify the client that its turn has started
     * @throws RemoteException
     */
    @Override
    public void startTurn() throws RemoteException {
        clientRMI.setMyTurnFlag(true);
        System.out.println("Your turn has started, press enter to play your turn");
    }

    /**
     * this method is called by the server to notify the client that he has received a chat message
     * @param text content of the message
     * @param sender name of the player that sent this message
     * @param pm bollean value, true if the message is private
     * @throws RemoteException
     */
    @Override
    public void receiveMessage(String text, String sender, Boolean pm) throws RemoteException {
        if(pm)
            System.out.println("[MESSAGE FROM "+sender+" TO YOU]: "+text);
        else
            System.out.println("[MESSAGE FROM "+sender+"]: "+text);
    }
    public void sendChatMessage(String message)throws RemoteException{

    }

    /**
     * this method is called by the server to notify the client that he sent a command with invalid parameters
     * @throws RemoteException
     */
    @Override
    public void invalidCommandSent() throws RemoteException {
        System.out.println("The command you sent has invalid parameters");
    }


    @Override
    public void updateBoard(TilePlacingSpot[][] boardView) throws RemoteException {

    }

    @Override
    public void updateOppShelf(String nickname, Tile[][] grid) throws RemoteException {

    }
    public void notifyOfDisconnection() throws RemoteException{
        synchronized (this){
            notifyAll();
        }
    }

    /**
     * this method is called by the server to show a broadcast message
     * @param message content of the message
     * @throws RemoteException
     */
    public void broadcastedMessage(String message) throws RemoteException{
        System.out.println(message);
    }

    @Override
    public void updateCommonGoalTokens() throws RemoteException {

    }
}
