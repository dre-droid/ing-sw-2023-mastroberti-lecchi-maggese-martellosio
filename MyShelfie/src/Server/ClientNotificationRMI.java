package Server;

import main.java.it.polimi.ingsw.Model.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientNotificationRMI extends java.rmi.server.UnicastRemoteObject implements ClientNotificationInterfaceRMI, Runnable{

    Object turnEnabler;
    protected ClientNotificationRMI() throws RemoteException {
        super();
        turnEnabler = new Object();
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

   /* @Override
    public void myTurnIsOver() throws RemoteException{
        synchronized (turnEnabler){
            try{
                turnEnabler.wait();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

    }*/



   /* @Override
    public void  myTurnIsStarting() throws RemoteException{
        synchronized (turnEnabler){
            turnEnabler.notifyAll();
            System.out.println("enabled to play");
        }

    }*/


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

    @Override
    public void announceCommonGoals(String commonGoals) throws RemoteException {
        System.out.println("The common goals for the game are: "+commonGoals);
    }

    @Override
    public void runOutOfTime() throws RemoteException {
        System.out.println("Sorry, you run out of time and lost the turn!");
    }

    @Override
    public void run() {
        try{
            int x,y,amount,directionInput,column,tileToBeRearranged;
            Board.Direction direction=null;
            List<Tile> drawnTiles=null;
            List<Integer> alreadyInsertedTiles;
            boolean correctlyDrawn=false;
            boolean correctlyInserted=false;
            boolean correctlyRearranged = false;
            List<Tile> rearrangedTiles=null;

            Registry registryServer = LocateRegistry.getRegistry("192.168.10.106");
            RMIinterface serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");

            Scanner userInput = new Scanner(System.in);
            System.out.println("Insert the port number: ");
            int myport = Integer.parseInt(userInput.nextLine());

            ClientNotificationRMI notifications = new ClientNotificationRMI();
            Registry registryNotifications = LocateRegistry.createRegistry(myport);
            registryNotifications.rebind("Client",notifications);



            System.out.println("Now insert the nickname for the game: ");
            int returnCode;
            String nickname;
            do{
                nickname = userInput.nextLine();
                returnCode = serverRMI.joinGame(nickname,myport);
                switch(returnCode){
                    case -1: {
                        System.out.println("Creating a new game...How many players can join your game?");
                        int numPlayers = Integer.parseInt(userInput.nextLine());
                        if(serverRMI.createNewGame(nickname,numPlayers,myport)){
                            returnCode=0;
                        }
                        else{
                            System.out.println("Somebody already created the game, try to join again");
                        }
                    }break;
                    case -2:{
                        System.out.println("Try again later");
                    }break;
                    case -3:{
                        System.out.println("Try a different nickname");
                    }break;
                    default:;
                }
            }while(returnCode!=0);
            if(!serverRMI.hasGameStarted())
                System.out.println("waiting for players...");


            /*synchronized (turnEnabler){
                try{
                    turnEnabler.wait();
                }catch (InterruptedException e){ e.printStackTrace();}
            }*/

            while(!serverRMI.hasGameStarted()){}
            //the game starts!

            do{
                while(serverRMI.isMyTurn(nickname)){
                    /*while (!serverRMI.isMyTurn(nickname)){
                        synchronized (turnEnabler){
                            try{
                                turnEnabler.wait();
                            }catch (InterruptedException e){ e.printStackTrace();}
                        }

                    }*/
                    waitForNotifications();
                    rearrangedTiles = new ArrayList<>();
                    drawnTiles = new ArrayList<>();
                    System.out.println("Here is the board: ");
                    printBoard(serverRMI.getBoard());
                    do{
                        do{
                            System.out.println("Select the row from which to draw from:");
                            x=Integer.parseInt(userInput.nextLine());}
                        while(x<0 || x>8);
                        do{
                            System.out.println("Select the column from which to draw from:");
                            y=Integer.parseInt(userInput.nextLine());}
                        while(y<0 || y>8);
                        do{
                            System.out.println("How many tiles do you want to draw?");
                            amount=Integer.parseInt(userInput.nextLine());}
                        while(amount<0 || amount>8);
                        do {
                            System.out.println("In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT");
                            directionInput = Integer.parseInt(userInput.nextLine());
                            switch(directionInput){
                                case 0: direction = Board.Direction.UP;break;
                                case 1: direction = Board.Direction.DOWN;break;
                                case 2: direction = Board.Direction.RIGHT;break;
                                case 3: direction = Board.Direction.LEFT;break;
                            }
                        }while(directionInput<0 || directionInput>3);
                        try{
                            drawnTiles = serverRMI.drawTilesFromBoard(nickname,x,y,amount,direction);
                            correctlyDrawn =true;
                        }catch(InvalidMoveException e){
                            System.out.println("You cannot draw those Tiles, try again!");
                            correctlyDrawn=false;
                        }
                    }while(!correctlyDrawn);
                    waitForNotifications();
                    System.out.println("Here are your tiles");
                    for(int i=0;i<drawnTiles.size();i++){
                        System.out.print((i+1)+") "+drawnTiles.get(i).getType()+"  ");
                    }
                    System.out.println();
                    System.out.println("Here is your Shelf: ");
                    printShelf(serverRMI.getMyShelf(nickname));

                    do{
                        alreadyInsertedTiles = new ArrayList<Integer>();
                        System.out.println("Choose in which column you want to insert the tiles: ");
                        column = Integer.parseInt(userInput.nextLine());
                        System.out.println("Now choose in which order you want to insert the tiles");
                        for(int i=0;i<drawnTiles.size();i++){
                            do{
                                System.out.println("Select the next to insert in the column: ");
                                do{
                                    tileToBeRearranged=-1;
                                    do{
                                        try{

                                            tileToBeRearranged = Integer.parseInt(userInput.nextLine());
                                        }catch(Exception e){
                                            System.out.println("Choose a valid tile by using the number connected to those (check the previous lines)");

                                        }
                                        if(tileToBeRearranged<0 || tileToBeRearranged>amount)
                                            System.out.println("Insert a number between 1 and "+amount);
                                    }while(tileToBeRearranged==-1);
                                }while(tileToBeRearranged<0 || tileToBeRearranged >amount);

                                int finalTileToBeRearranged = tileToBeRearranged;
                                if(alreadyInsertedTiles.stream().noneMatch((num)->(num== finalTileToBeRearranged))){
                                    alreadyInsertedTiles.add(tileToBeRearranged);
                                    rearrangedTiles.add(0,drawnTiles.get(tileToBeRearranged-1));
                                    correctlyRearranged=true;
                                }else{
                                    System.out.println("You already inserted this tile, try again");
                                }
                            }while(!correctlyRearranged);
                            correctlyRearranged=false;
                        }
                        correctlyInserted= serverRMI.insertTilesInShelf(nickname,rearrangedTiles,column-1);
                        //System.out.println(correctlyInserted);
                    }while(!correctlyInserted);
                    waitForNotifications();
                    System.out.println("Shelf after the tiles are inserted: ");
                    printShelf(serverRMI.getMyShelf(nickname));
                    waitForNotifications();
                    serverRMI.checkIfCommonGoalsHaveBeenFulfilled(nickname);
                    waitForNotifications();
                    serverRMI.endOfTurn(nickname);
                    waitForNotifications();
                    System.out.println("Your turn has ended, you now have "+serverRMI.getPoints(nickname)+" pts");
                }
            }while(!serverRMI.isGameOver());
            //serverRMI.joinGame(nickname,myport);
        }catch(RemoteException | NotBoundException e){
            e.printStackTrace();
        }
    }

    public static void printShelf(Tile[][] grid){
        for(int i = 5;i>=0;i--) {
            for (int j = 0; j < 5; j++) {
                if (grid[i][j]==null) System.out.print("O ");
                else{
                    switch(grid[i][j].getType()){
                        case CAT: System.out.print("C ");break;
                        case BOOK:System.out.print("B ");break;
                        case GAME:System.out.print("G ");break;
                        case FRAME:System.out.print("F ");break;
                        case PLANT:System.out.print("P ");break;
                        case TROPHY:System.out.print("T ");break;
                    }
                }
            }
            System.out.println();
        }
        System.out.println("");
    }

    public static void printBoard(TilePlacingSpot[][] grid){
        for(int i = 0;i<9;i++) {
            for (int j = 0; j < 9; j++) {
                if (!grid[i][j].isAvailable()) System.out.print("X ");
                else{
                    if(grid[i][j].isEmpty()) System.out.print("e ");
                    else{
                        Tile t = grid[i][j].showTileInThisPosition();
                        switch(t.getType()){
                            case CAT: System.out.print("C ");break;
                            case BOOK:System.out.print("B ");break;
                            case GAME:System.out.print("G ");break;
                            case FRAME:System.out.print("F ");break;
                            case PLANT:System.out.print("P ");break;
                            case TROPHY:System.out.print("T ");break;
                        }
                    }

                }
            }
            System.out.println("");
        }
    }

    public static void waitForNotifications(){
        try{
            TimeUnit.MILLISECONDS.sleep(300);
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
