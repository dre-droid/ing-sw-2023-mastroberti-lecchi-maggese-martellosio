package Server.RMI;

import main.java.it.polimi.ingsw.Model.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
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
    public void someoneHasCompletedACommonGoal(String playerNickname, String commongoal) throws RemoteException {
        System.out.println(playerNickname+" has completed a common goal, congratulations!");
        System.out.println("The common goal completed is:");
        System.out.println(commongoal);
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
        System.out.println("The common goals for the game are: \n"+commonGoals);
    }

    @Override
    public void runOutOfTime() throws RemoteException {
        System.out.println("Sorry, you run out of time and lost the turn, type anything to continue!");
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

            boolean drawIsOver=false;
            boolean insertIsOver=false;

            final boolean[] drawInTime = {true};

            Registry registryServer = LocateRegistry.getRegistry();
            RMIinterface serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");


            Scanner userInput = new Scanner(System.in);
            int myport=-1;
            ClientNotificationRMI notifications = new ClientNotificationRMI();
            Registry registryNotifications;
            System.out.println("Insert the port number (to receive notifications): ");
            do{
                try{
                    myport = Integer.parseInt(userInput.nextLine());
                }catch(Exception e){
                    myport=-1;
                }
                try{
                    registryNotifications = LocateRegistry.createRegistry(myport);
                    registryNotifications.rebind("Client",notifications);
                }catch (Exception e){
                    myport=-1;
                }
                if(myport==-1)
                    System.out.println("Insert a valid value for the port number!");
            }while (myport==-1);







            System.out.println("Now insert the nickname for the game: ");
            int returnCode;
            String nickname;

            do{
                nickname = userInput.nextLine();
                returnCode = serverRMI.joinGame(nickname,myport);
                switch(returnCode){
                    case -1: {
                        System.out.println("Creating a new game...How many players can join your game? (2, 3, 4)");
                        int numPlayers;
                        do{
                            numPlayers=-1;
                            try{
                                numPlayers = Integer.parseInt(userInput.nextLine());
                            }catch (Exception e){
                                numPlayers=-1;
                            }
                            if(numPlayers<2 || numPlayers>4)
                                System.out.println("Insert a valid value for the number of players (2, 3, 4)");
                        }while(numPlayers<2 || numPlayers>4);


                        if(serverRMI.createNewGame(nickname,numPlayers,myport)){
                            returnCode=0;
                        }
                        else{
                            System.out.println("Somebody already created the game, try to join again, insert your nickname");
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

            while(!serverRMI.isGameOver()){
                while(serverRMI.isMyTurn(nickname)){
                    /*while (!serverRMI.isMyTurn(nickname)){
                        synchronized (turnEnabler){
                            try{
                                turnEnabler.wait();
                            }catch (InterruptedException e){ e.printStackTrace();}
                        }

                    }*/

                    userInput = new Scanner(System.in);

                    waitForNotifications();
                    rearrangedTiles = new ArrayList<>();
                    drawnTiles = new ArrayList<>();
                    System.out.println("Here is the board: ");
                    printBoard(serverRMI.getBoard());

                    Timer timerForTheDrawing = new Timer();
                    drawInTime[0]=true;
                    timerForTheDrawing.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            drawInTime[0] =false;
                        }
                    },60000);
                    do{
                        //read value for the row
                        System.out.println("Select the row from which to draw from:");
                        x = -1;
                        if(drawInTime[0]){
                            do{
                                x=-1;
                                try {
                                    x = Integer.parseInt(userInput.nextLine());
                                } catch (Exception e) {
                                    //System.out.println("Insert a valid value for the row (0, 1, 2, ..., 8)");
                                }
                                if((x<0 || x>8) && drawInTime[0])
                                    System.out.println("Insert a valid value for the row (0, 1, 2, ..., 8)");
                            } while((x<0 || x>8) && drawInTime[0]);
                        }


                        //read value for the column
                        System.out.println("Select the column from which to draw from:");
                        y = -1;
                        if(drawInTime[0]){
                            do{
                                y=-1;
                                try {
                                    y = Integer.parseInt(userInput.nextLine());
                                } catch (Exception e) {
                                    //System.out.println("Insert a valid value for the column (0, 1, 2, ..., 8)");
                                }
                                if((y<0 || y>8) && drawInTime[0])
                                    System.out.println("Insert a valid value for the column (0, 1, 2, ..., 8)");
                            } while((y<0 || y>8) && drawInTime[0]);
                        }

                        //read the value for the amount
                        System.out.println("How many tiles do you want to draw?");
                        amount = -1;
                        if(drawInTime[0]){
                            do{
                                amount=-1;
                                try {
                                    amount = Integer.parseInt(userInput.nextLine());
                                } catch (Exception e) {
                                    //System.out.println("Insert a valid value for the amount (1, 2, .., 6)");
                                }
                                if((amount<0 || amount>6) && drawInTime[0])
                                    System.out.println("Insert a valid value for the amount (1, 2, .., 6)");
                            } while((amount<0 || amount>6) && drawInTime[0]);
                        }


                        //read the value for the direction

                        System.out.println("In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                        directionInput = -1;
                        if(drawInTime[0]){
                            do{
                                directionInput=-1;
                                try {
                                    directionInput = Integer.parseInt(userInput.nextLine());
                                } catch (Exception e) {
                                    //System.out.println("Insert a valid value for the direction (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                                }
                                if((directionInput<0 || directionInput>3) && drawInTime[0])
                                    System.out.println("Insert a valid value for the direction (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");

                            } while((directionInput<0 || directionInput>3) && drawInTime[0]);
                        }
                        if(drawInTime[0]){
                            switch(directionInput){
                                case 0: direction = Board.Direction.UP;break;
                                case 1: direction = Board.Direction.DOWN;break;
                                case 2: direction = Board.Direction.RIGHT;break;
                                case 3: direction = Board.Direction.LEFT;break;
                            }
                        }

                        if(drawInTime[0]){
                            drawnTiles = serverRMI.drawTilesFromBoard(nickname,x,y,amount,direction);
                            if(drawnTiles !=null){
                                correctlyDrawn =true;
                            }
                            else {
                                System.out.println("You cannot draw those Tiles, try again!");
                                correctlyDrawn=false;
                            }
                        }
                    }while(!correctlyDrawn && drawInTime[0]);
                    timerForTheDrawing.cancel();


                    //if the first action has been completed in time we continue with the rest of the turn
                    if(correctlyDrawn){
                        final boolean[] insertInTime = {true};
                        Timer timerForTheInsert = new Timer();
                        timerForTheInsert.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                insertInTime[0] =false;
                            }
                        },70000);
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
                            //read the value for the column
                            column = -1;
                            if(insertInTime[0]){
                                System.out.println("Choose in which column you want to insert the tiles: ");
                                do{
                                    column=-1;
                                    try {
                                        column = Integer.parseInt(userInput.nextLine());
                                    } catch (Exception e) {
                                        //System.out.println("Insert a valid value for the column (0, 1, 2, ..., 8)");
                                    }
                                    if(!insertInTime[0])
                                        System.out.println("Type anything to conclude the turn");
                                    if((column<1 || column>5) && insertInTime[0])
                                        System.out.println("Insert a valid value for the column (1, 2, ..., 5)");
                                } while((column<1 || column>5) && insertInTime[0]);
                            }

                            if(insertInTime[0]){
                                System.out.println("Now choose in which order you want to insert the tiles");
                                for(int i=0;i<drawnTiles.size();i++){
                                    if(insertInTime[0]){
                                        do{
                                            System.out.println("Select the next to insert in the column: ");
                                            do{
                                                tileToBeRearranged=-1;
                                                try{
                                                    tileToBeRearranged = Integer.parseInt(userInput.nextLine());
                                                }catch(Exception e){
                                                    System.out.println("Choose a valid tile by using the number connected to those (check the previous lines)");

                                                }
                                                if(!insertInTime[0])
                                                    System.out.println("Time has run out, type anything to end your turn");
                                                if((tileToBeRearranged<0 || tileToBeRearranged >amount) && insertInTime[0])
                                                    System.out.println("Insert a number between 1 and "+amount);
                                            }while((tileToBeRearranged<0 || tileToBeRearranged >amount) && insertInTime[0]);

                                            if(insertInTime[0]){
                                                int finalTileToBeRearranged = tileToBeRearranged;
                                                if(alreadyInsertedTiles.stream().noneMatch((num)->(num== finalTileToBeRearranged))){
                                                    alreadyInsertedTiles.add(tileToBeRearranged);
                                                    rearrangedTiles.add(0,drawnTiles.get(tileToBeRearranged-1));
                                                    correctlyRearranged=true;
                                                }else{
                                                    System.out.println("You already inserted this tile, try again");
                                                }
                                            }

                                        }while(!correctlyRearranged && insertInTime[0]);
                                        correctlyRearranged=false;
                                    }
                                }
                            }
                            if(insertInTime[0]){
                                correctlyInserted= serverRMI.insertTilesInShelf(nickname,rearrangedTiles,column-1);
                            }

                            //System.out.println(correctlyInserted);
                        }while(!correctlyInserted && insertInTime[0]);
                        timerForTheInsert.cancel();
                        waitForNotifications();
                        if(correctlyDrawn && correctlyInserted){
                            System.out.println("Shelf at the end of the turn: ");
                            printShelf(serverRMI.getMyShelf(nickname));
                            //serverRMI.checkIfCommonGoalsHaveBeenFulfilled(nickname);;
                        }
                        waitForNotifications();
                        System.out.println("Your turn has ended, you now have "+serverRMI.getPoints(nickname)+" pts");
                    }
                }
            }
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
