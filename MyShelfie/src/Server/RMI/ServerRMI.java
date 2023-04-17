package Server.RMI;

import Server.Controller;
import main.java.it.polimi.ingsw.Model.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ServerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{

    Timer timerDraw;
    Timer timerInsert;

    Controller controller;

    private final int drawDelay= 60000;
    private final int insertDelay = 70000;
    private List<ClientNotificationRecord> clients;



    protected ServerRMI() throws RemoteException {super();
        clients = new ArrayList<ClientNotificationRecord>();
        timerDraw = new Timer();
        timerInsert = new Timer();
        controller = new Controller();
    }

    @Override
    public int joinGame(String nickname,int port) throws java.rmi.RemoteException {
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        int outcome = controller.joinGame(nickname);
        switch (outcome){
            case 0: {
                clientToBeNotified.gameJoinedCorrectlyNotification();
                clients.add(new ClientNotificationRecord(nickname, clientToBeNotified));
                for (ClientNotificationRecord clientNotificationRecord : clients) {
                    clientNotificationRecord.client.someoneJoinedTheGame(nickname);
                }
                if (controller.hasGameStarted()) {
                    startTimer(timerDraw, drawDelay);
                    String commonGoals;
                    commonGoals = controller.getCommonGoalCard1Description()+"\n"+controller.getCommonGoalCard2Description();
                    for (ClientNotificationRecord clientNotificationRecord : clients) {
                        clientNotificationRecord.client.startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                        clientNotificationRecord.client.announceCommonGoals(commonGoals);
                    }
                    break;
                }
            }
            case -1: {
                clientToBeNotified.problemInJoiningGame("There is no game to join");
            }break;
            case -2: {
                clientToBeNotified.problemInJoiningGame("Sorry, the game has already started");
            }break;
            case -3: {
                clientToBeNotified.problemInJoiningGame("The nickname you chose is already being used");
            }break;
        }
        return outcome;
    }


    @Override
    public boolean createNewGame(String nickname, int numOfPlayers,int port) throws java.rmi.RemoteException{
        if(numOfPlayers<0 || numOfPlayers>4)
            return false;
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if(controller.createNewGame(nickname, numOfPlayers)){
            clientToBeNotified.gameCreatedCorrectly();
            clients.add(new ClientNotificationRecord(nickname,clientToBeNotified));
            System.out.println("Created new game by "+nickname);
            return true;
        }
        clientToBeNotified.cannotCreateNewGame("There is already a game to join");
        System.out.println("There is already a game to join");
        return false;
    }


    @Override
    public List<Tile> drawTilesFromBoard(String playerNickname, int x,int y,int amount,Board.Direction direction) throws java.rmi.RemoteException{
        List<Tile> drawnTiles = controller.drawFromBoard(playerNickname, x, y, amount, direction);
        if(drawnTiles==null){
            clients.stream().filter(client->client.nickname.equals(playerNickname)).toList().get(0).client.moveIsNotValid();
            return null;
        }
        timerDraw.cancel();
        timerInsert = new Timer();
        startTimer(timerInsert,insertDelay);
        return drawnTiles;
    }

    @Override
    public boolean hasGameStarted() throws RemoteException {
        return controller.hasGameStarted();
    }

    @Override
    public Tile[][] getMyShelf(String playerNickname) throws RemoteException {
        return controller.getMyShelf(playerNickname);
    }

    public boolean isMyTurn(String playerNickname) throws RemoteException{
        return controller.isMyTurn(playerNickname);
    }

    @Override
    public TilePlacingSpot[][] getBoard() throws RemoteException {
        return controller.getBoard();
    }

    @Override
    public boolean insertTilesInShelf(String playernickName, List<Tile> tiles, int column) throws RemoteException{
        if(controller.insertTilesInShelf(playernickName, tiles, column)){
            timerInsert.cancel();
            endOfTurn(playernickName);
            return true;
        }
        else {
            clients.stream().filter(cl->cl.nickname.equals(playernickName)).toList().get(0).client.moveIsNotValid();
            return false;
        }
    }


    private void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())) {
            if (controller.checkIfCommonGoalN1IsFulfilled(playerNickname)) {
                for (ClientNotificationRecord c : clients) {
                    c.client.someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard1Description());
                }
            }
            if (controller.checkIfCommonGoalN2IsFulfilled(playerNickname))
                for (ClientNotificationRecord c : clients) {
                    c.client.someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard2Description());
                }
        }
    }


    private void endOfTurn(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())){
            checkIfCommonGoalsHaveBeenFulfilled(playerNickname);
            controller.endOfTurn(playerNickname);
            for(ClientNotificationRecord c: clients){
                c.client.aTurnHasEnded(playerNickname, controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            }
            if(controller.hasTheGameEnded()){
                for(ClientNotificationRecord c: clients)
                    c.client.gameIsOver(controller.getLeaderboard());
            }
            else{
                timerDraw = new Timer();
                startTimer(timerDraw,drawDelay);
            }
        }
    }

    private void startTimer(Timer timer, int delay){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    clients.stream().filter(client -> client.nickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())).toList().get(0).client.runOutOfTime();
                    endOfTurn(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        },delay);
    }




    @Override
    public int getPoints(String playerNickname) throws RemoteException {
        return controller.getPoints(playerNickname);
    }

    @Override
    public boolean isGameOver() throws RemoteException {
        return controller.hasTheGameEnded();
    }




    public static void main(String[] args){
        try{
            ServerRMI server = new ServerRMI();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MyShelfie",server);
        }catch(Exception e){
            e.printStackTrace();

        }

    }
}
