package GUI;

import Server.RMI.ClientNotificationInterfaceRMI;
import Server.RMI.ClientNotificationRMI;
import Server.RMI.RMIinterface;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClientNotificationRMIGUI extends java.rmi.server.UnicastRemoteObject implements ClientNotificationInterfaceRMI {
    private RMIinterface serverRMI;

    GameSceneController gsc;

    private int port;

    private String nickname;
    private boolean MyTurnFlag;
    private boolean EndGameFlag;
    private boolean GameStartFlag;

    public ClientNotificationRMIGUI() throws RemoteException{
        try{
            Registry registryServer = LocateRegistry.getRegistry();
            serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");
        }catch(RemoteException | NotBoundException e){
            System.out.println("ClientNotificationRMIGUI--> ERROR: cannot connect to server");
        }
        MyTurnFlag = false;
        EndGameFlag = false;
        GameStartFlag = false;
    }

    public void startNotificationServer() throws RemoteException{
        Random random = new Random();
        port = random.nextInt(3000,6000);
        Registry clientRegistry = LocateRegistry.createRegistry(port);
        clientRegistry.rebind("Client",this);
    }

    public void setnickname(String nick){
        this.nickname = nick;
    }

    public String getNickname(){return this.nickname;}

    public int joinGame() throws RemoteException{
        System.out.println("trying login");
        return serverRMI.joinGame(nickname, port);
    }

    public boolean createNewGame(int numOfPlayers) throws RemoteException{
        System.out.println("creating new game...");
        return serverRMI.createNewGame(nickname, numOfPlayers, port);
    }



    @Override
    public void gameJoinedCorrectlyNotification() throws RemoteException {

    }

    @Override
    public void problemInJoiningGame(String problem) throws RemoteException {

    }

    @Override
    public void gameCreatedCorrectly() throws RemoteException {

    }

    @Override
    public void cannotCreateNewGame(String problem) throws RemoteException {

    }

    @Override
    public void someoneJoinedTheGame(String nickname) throws RemoteException {

    }

    @Override
    public void startingTheGame(String startingPlayer) throws RemoteException {
        GameStartFlag=true;
        updateGUIAtBeginningOfGame();
    }

    public void updateGUIAtBeginningOfGame(){
        try{
            TilePlacingSpot[][] board= serverRMI.getBoard();
            Map<Integer, PersonalGoalCard> pgcMap = serverRMI.getPGCmap();
            PersonalGoalCard pgc = serverRMI.getPGC(nickname);
            List<CommonGoalCard> cgcs = serverRMI.getCommonGoalCards();

            if(gsc!=null) {
                gsc.updateBoard(board);
                gsc.setPersonalGoalCardImage(pgc, pgcMap);
                gsc.createLeaderboard(serverRMI.getLeaderboard());
                gsc.setCommonGoalCardImage(cgcs.get(0),1);
                gsc.setCommonGoalCardImage(cgcs.get(1),2);
            }
        }catch(RemoteException re){
            System.out.println("Problem in the update of the gui at the beginning of the game");
            re.printStackTrace();
        }
    }




    @Override
    public void someoneHasCompletedACommonGoal(String playerNickname, String commongoal) throws RemoteException {

    }

    @Override
    public void aTurnHasEnded(String currentPlayerNickname, String nextPlayerNickname) throws RemoteException {

    }

    @Override
    public void gameIsOver(List<String> leaderboard) throws RemoteException {

    }

    @Override
    public void moveIsNotValid() throws RemoteException {

    }

    @Override
    public void announceCommonGoals(String commonGoals) throws RemoteException {

    }

    @Override
    public void runOutOfTime() throws RemoteException {

    }

    @Override
    public void ping() throws RemoteException {

    }

    @Override
    public void startTurn() throws RemoteException {

    }

    @Override
    public void receiveMessage(String text, String sender) throws RemoteException {

    }

    @Override
    public void invalidCommandSent() throws RemoteException {

    }

    public void setGameSceneController(GameSceneController controller){
        gsc = controller;
    }

    public TilePlacingSpot[][] getBoard(){
        try {
            return serverRMI.getBoard();
        } catch (RemoteException e) {
            System.out.println("problem in getting the board");
            return null;
        }

    }
}
