package Server;

import main.java.it.polimi.ingsw.Model.*;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.stream.Collectors;

public class ControllerRMI extends java.rmi.server.UnicastRemoteObject implements RMIinterface{

    private Game game;
    private List<ClientNotificationRecord> clients;



    protected ControllerRMI() throws RemoteException {super();
        game = null;
        clients = new ArrayList<ClientNotificationRecord>();
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
        if(game==null) {
            System.out.println("There is no game to join, create a new one "+nickname);
            clientToBeNotified.problemInJoiningGame("There is no game to join");
            return -1;
        }
        if(game.hasGameStarted()){
            System.out.println("The game has alredy started, "+nickname+" can't join");
            clientToBeNotified.problemInJoiningGame("Sorry, the game has already started");
            return -2;
        }
        if(game.addPlayer(nickname)){
            System.out.println(nickname+" joined the game");
            clientToBeNotified.gameJoinedCorrectlyNotification();
            clients.add(new ClientNotificationRecord(nickname,clientToBeNotified));
            for(ClientNotificationRecord clientNotificationRecord:clients){
                clientNotificationRecord.client.someoneJoinedTheGame(nickname);
            }
            if(game.hasGameStarted()){
                for(ClientNotificationRecord clientNotificationRecord:clients){
                    clientNotificationRecord.client.startingTheGame(game.isPlaying.getNickname());
                }
            }
            return 0;
        }
        else{
            System.out.println("Nickname alredy used");
            clientToBeNotified.problemInJoiningGame("The nickname you chose is already being used");
            return -3;
        }

    }

    @Override
    public boolean createNewGame(String nickname, int numOfPlayers,int port) throws java.rmi.RemoteException{
        ClientNotificationInterfaceRMI clientToBeNotified;
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            clientToBeNotified = (ClientNotificationInterfaceRMI) registry.lookup("Client");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if(game==null){
            game = new Game(numOfPlayers);
            game.addPlayer(nickname);
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
    public List<Tile> drawTilesFromBoard(String playerNickname, int x,int y,int amount,Board.Direction direction) throws java.rmi.RemoteException, InvalidMoveException{
        if(game.isPlaying.getNickname().equals(playerNickname)){
            return game.isPlaying.drawTiles(x,y,amount,direction);
        }
        return null;
    }

    @Override
    public boolean hasGameStarted() throws RemoteException {
        return game.hasGameStarted();
    }

    @Override
    public Tile[][] getMyShelf(String playerNickname) throws RemoteException {
        if(game.hasGameStarted()){
            Tile[][] displayGrid = new Tile[6][5];
            for(Player p: game.getPlayerList()){
                if(p.getNickname().equals(playerNickname))
                    displayGrid = p.getShelf().getGridForDisplay();
            }
            return displayGrid;
        }
        return null;
    }

    public boolean isMyTurn(String playerNickname) throws RemoteException{
        return game.isPlaying.getNickname().equals(playerNickname);
    }

    @Override
    public TilePlacingSpot[][] getBoard() throws RemoteException {
        return game.getBoard().getBoardForDisplay();
    }

    @Override
    public boolean insertTilesInShelf(String playernickName, List<Tile> tiles, int column) throws RemoteException{
        if(playernickName.equals(game.isPlaying.getNickname())){
            if(column<0 || column>5)
                return false;
            try{
                game.insertTilesInShelf(tiles,column,game.isPlaying);
            }catch(InvalidMoveException e){
                clients.stream().filter(cl->cl.nickname.equals(playernickName)).toList().get(0).client.moveIsNotValid();
                return false;
            }

        }
        return false;
    }

    @Override
    public void checkIfCommonGoalsHaveBeenFulfilled(String playerNickname) throws RemoteException {
        if(playerNickname.equals(game.isPlaying.getNickname())){
            if(game.checkIfCommonGoalN1IsFulfilled(game.isPlaying))
                clients.stream().filter(cl -> cl.nickname.equals(playerNickname)).toList().get(0).client.someoneHasCompletedACommonGoal(playerNickname);
            if(game.checkIfCommonGoalN2IsFulfilled(game.isPlaying))
                clients.stream().filter(cl -> cl.nickname.equals(playerNickname)).toList().get(0).client.someoneHasCompletedACommonGoal(playerNickname);
        }
    }

    @Override
    public void endOfTurn(String playerNickname) throws RemoteException {
        if(playerNickname.equals(game.isPlaying.getNickname())){
            game.endOfTurn(game.isPlaying);
            for(ClientNotificationRecord c: clients){
                c.client.aTurnHasEnded(playerNickname,game.isPlaying.getNickname());
            }
            if(game.hasTheGameEnded()){
                for(ClientNotificationRecord c: clients)
                    c.client.gameIsOver(game.getLeaderBoard());
            }
        }

    }

    @Override
    public int getPoints(String playerNickname) throws RemoteException {
        return game.getPlayerList().stream().filter(player -> player.getNickname().equals(playerNickname)).toList().get(0).getScore();
    }

    @Override
    public boolean isGameOver() throws RemoteException {
        return game.hasTheGameEnded();
    }


    public static void main(String[] args){
        try{
            ControllerRMI server = new ControllerRMI();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MyShelfie",server);
        }catch(Exception e){
            e.printStackTrace();

        }

    }
}
