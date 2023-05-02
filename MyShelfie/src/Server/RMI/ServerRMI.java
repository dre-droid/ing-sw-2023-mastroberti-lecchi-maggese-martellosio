package Server.RMI;

import Server.Controller;
import Server.Server;
import com.google.gson.Gson;
import main.java.it.polimi.ingsw.Model.*;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ServerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{

    //Timer timerDraw;
    //Timer timerInsert;

    Server server;
    Controller controller;

    private final int drawDelay= 60000;
    private final int insertDelay = 70000;
    //private List<ClientNotificationRecord> clients;

    private Map<String, ClientNotificationInterfaceRMI> clients;



    public ServerRMI(Controller controller, Server server) throws RemoteException {super();
        clients = new HashMap();
        //timerDraw = new Timer();
        //timerInsert = new Timer();
        this.controller = controller;
        this.server = server;

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
                clients.put(nickname, clientToBeNotified);
                //clients.add(new ClientNotificationRecord(nickname, clientToBeNotified));
                /*for (ClientNotificationRecord clientNotificationRecord : clients) {
                    clientNotificationRecord.client.someoneJoinedTheGame(nickname);
                }*/
                server.addPlayerToRecord(nickname, Server.connectionType.RMI);
                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    client.getValue().someoneJoinedTheGame(nickname);
                }
                if (controller.hasGameStarted()) {
                    /*startTimer(timerDraw, drawDelay);*/
                    notifyStartOfGame();
                    /*for (ClientNotificationRecord clientNotificationRecord : clients) {
                        clientNotificationRecord.client.startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                        clientNotificationRecord.client.announceCommonGoals(commonGoals);
                    }*/
                }
            }break;
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
            //clients.add(new ClientNotificationRecord(nickname,clientToBeNotified));
            clients.put(nickname, clientToBeNotified);
            server.addPlayerToRecord(nickname, Server.connectionType.RMI);
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
            //clients.stream().filter(client->client.nickname.equals(playerNickname)).toList().get(0).client.moveIsNotValid();
            clients.get(playerNickname).moveIsNotValid();
            return null;
        }
        /*timerDraw.cancel();
        timerInsert = new Timer();
        startTimer(timerInsert,insertDelay);*/
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
            //timerInsert.cancel();
            endOfTurn(playernickName);
            return true;
        }
        else {
            clients.get(playernickName).moveIsNotValid();
            //clients.stream().filter(cl->cl.nickname.equals(playernickName)).toList().get(0).client.moveIsNotValid();
            return false;
        }
    }


    private void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())) {
            if (controller.checkIfCommonGoalN1IsFulfilled(playerNickname)) {

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard1Description());
                }

                /*for (ClientNotificationRecord c : clients) {
                    c.client.someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard1Description());
                }*/
            }
            if (controller.checkIfCommonGoalN2IsFulfilled(playerNickname)){

                for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                    client.getValue().someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard2Description());
                }

                /*for (ClientNotificationRecord c : clients) {
                    c.client.someoneHasCompletedACommonGoal(playerNickname, controller.getCommonGoalCard2Description());
                }*/
            }

        }
    }


    public void endOfTurn(String playerNickname) throws RemoteException {
        if(playerNickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())){
            checkIfCommonGoalsHaveBeenFulfilled(playerNickname);

            for(Map.Entry<String, ClientNotificationInterfaceRMI> client: clients.entrySet()){
                client.getValue().aTurnHasEnded(playerNickname, controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            }

            //ClientNotificationInterfaceRMI clientToBeNotified = clients.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
            /*if(clientToBeNotified!=null)
                clientToBeNotified.startTurn();*/
            controller.endOfTurn(playerNickname);

            if(controller.hasTheGameEnded()){
                notifyEndOfGame();
            }
            //saveGameProgress();

        }
    }

    /*private void startTimer(Timer timer, int delay){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    //clients.stream().filter(client -> client.nickname.equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying())).toList().get(0).client.runOutOfTime();
                    clients.get(controller.getNameOfPlayerWhoIsCurrentlyPlaying()).runOutOfTime();
                    endOfTurn(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        },delay);
    }*/




    @Override
    public int getPoints(String playerNickname) throws RemoteException {
        return controller.getPoints(playerNickname);
    }

    @Override
    public boolean isGameOver() throws RemoteException {
        return controller.hasTheGameEnded();
    }

    @Override
    public boolean reconnect(String playerNickname, int port)  throws RemoteException{
        //we check if there is a player with this name in the game
        if(clients.containsKey(playerNickname)){
            //we check if the client is already connected with a ping, if the client is not connected we proceed to the reconnection
            try{
                clients.get(playerNickname).ping();
                System.out.println("I can still ping "+playerNickname);
                return false;
            } catch (RemoteException e) {
                //we update the list of the clients with the new client if we can connect to it
                ClientNotificationInterfaceRMI clientToBeNotified;
                try{
                    Registry registry = LocateRegistry.getRegistry(port);
                    clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
                } catch (NotBoundException | java.rmi.RemoteException  ex) {
                    System.out.println("Cannot connect to the new client");
                    return false;
                }
                clients.put(playerNickname, clientToBeNotified);
                System.out.println("Connected to the new client");
                return true;
            }
        }
        else{
            return false;
        }
    }

    @Override
    public Tile[][] getMyPersonalGoal(String playerNickname) throws RemoteException {
        return controller.getMyPersonalCard(playerNickname);
    }

    @Override
    public String getCommonGoalCardDescription() throws RemoteException {
        return controller.getCommonGoalCard1Description()+"\n"+controller.getCommonGoalCard2Description();
    }


    public void flushServer(){

    }

    public void notifyStartOfTurn(String playerNickname){
        try{
            ClientNotificationInterfaceRMI clientToBeNotified;
            clientToBeNotified = clients.get(playerNickname);
            if(clientToBeNotified!=null){
                clientToBeNotified.startTurn();
                System.out.println("Notified "+ playerNickname +"to start the turn");
            }

        } catch (RemoteException e) {
            System.out.println("Cannot notify client: "+playerNickname);
        }

    }

    private void saveGameProgress() {
        try {
            controller.saveGameProgress();
        } catch (IOException e) {
            System.out.println("-----Problem in saving the game progress-----");
        }
    }

    public void notifyStartOfGame() {
        try{
            for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
                client.getValue().startingTheGame(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                client.getValue().announceCommonGoals(controller.getCommonGoalCard1Description()+"\n"+controller.getCommonGoalCard2Description());
                if(client.getKey().equals(controller.getNameOfPlayerWhoIsCurrentlyPlaying()))
                    notifyStartOfTurn(client.getKey());
            }
        } catch (RemoteException e) {
            System.out.println("Cannot notify client");
        }

    }

    public void notifyEndOfGame() {
        for (Map.Entry<String, ClientNotificationInterfaceRMI> client : clients.entrySet()) {
            try {
                List<String> leaderboard = controller.getLeaderboard().stream().map(player->player.getNickname()+" pts: "+player.getScore()).toList();
                client.getValue().gameIsOver(leaderboard);
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Cannot notify controller");
            }
        }
    }

    public void setController(Controller controller){
        this.controller = controller;
    }

    public void quitGame(String playerNickname) throws RemoteException{
        System.out.println("Server received quit command");
        controller.endGame();
    }

    @Override
    public void chatMessage(String senderName, String text, String receiverName) throws RemoteException {
        if(clients.get(receiverName)==null){
            server.chatMessage(senderName, text, receiverName);
        }
        else{
            clients.get(receiverName).receiveMessage(text, senderName);
        }
    }


}
