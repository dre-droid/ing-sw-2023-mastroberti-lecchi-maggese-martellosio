package Server.RMI;

import Server.ClientWithChoice;
import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.TilePlacingSpot;
import org.junit.jupiter.api.MethodOrderer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ClientRMI implements Runnable{

    private RMIinterface serverRMI;
    private ClientNotificationRMI notifications;
    private String playerNickname;

    private boolean MyTurnFlag;
    private boolean EndGameFlag;
    private boolean GameStartFlag;
    int myport;

    public ClientRMI(){
        startClientNotificationServer();
        MyTurnFlag = false;
        EndGameFlag = false;
        GameStartFlag = false;
    }

    public void setMyTurnFlag(boolean value){
        MyTurnFlag = value;
    }

    public boolean getMyTurnFlag(){
        return MyTurnFlag;
    }

    public void setEndGameFlag(boolean value){
        EndGameFlag = value;
    }

    public boolean getEndGameFlag(){
        return EndGameFlag;
    }

    public void setGameStartFlag(boolean value){
        GameStartFlag = value;
    }

    public boolean getGameStartFlag(){
        return GameStartFlag;
    }


    private boolean connectToRMIserver(String url){
        try{
            Registry registryServer = LocateRegistry.getRegistry(url);
            serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");
        }catch(RemoteException | NotBoundException e){
            return false;
        }
        return true;
    }

    private void startClientNotificationServer(){
        Random random = new Random();
        boolean outcome = false;
        do{
            try{
                myport = random.nextInt(3000,6000);
                notifications = new ClientNotificationRMI(this);
                Registry clientRegistry = LocateRegistry.createRegistry(myport);
                clientRegistry.rebind("Client",notifications);
                outcome = true;
            }catch (RemoteException e){
                outcome=false;
            }
        }while(!outcome);
    }

    private void joinGame(Scanner userInput) throws RemoteException {
        int returnCode;
        do{
            playerNickname = userInput.nextLine();
            returnCode = serverRMI.joinGame(playerNickname,myport);
            switch(returnCode){
                case -1: {
                    System.out.println("Enter 1 if you want to load the saved game progress, 2 if you want to create a new game");
                    int decisionAboutNewGame=-1;
                    decisionAboutNewGame=checkedInputForIntValues(userInput,1,2,"Enter 1 if you want to load the saved game progress, 2 if you want to create a new game");
                    if(decisionAboutNewGame==1){
                        if(serverRMI.loadGameProgressFromFile()){
                            System.out.println("Loaded the saved game progress on the server");
                            reconnectToGame(userInput);
                            return;
                        }
                        else{
                            System.out.println("There is no saved game progress, create a new game");
                        }
                    }

                    System.out.println("Creating a new game...How many players can join your game? (2, 3, 4)");
                    int numPlayers = checkedInputForIntValues(userInput,2,4,"Insert a valid value for the number of players (2, 3, 4)");

                    if(serverRMI.createNewGame(playerNickname,numPlayers,myport)){
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
            }
        }while(returnCode!=0);
    }

    private void reconnectToGame(Scanner userInput){
        System.out.println("Enter the nickname you used last time to join the game");
        playerNickname = userInput.nextLine();
        boolean reconnected = false;
        do{
            try{
                reconnected= serverRMI.reconnect(playerNickname, myport);
            }catch(RemoteException e){
            }

            if(!reconnected){
                System.out.println("Cannot reconnect to the game, someone is already connected with this nickname, try with another one");
                playerNickname = userInput.nextLine();
            }
        }while(!reconnected);
        System.out.println("You reconnected to the game, wait for your turn now");
    }


    private int checkedInputForIntValues(Scanner scanner, int min, int max, String ErrorMessage){
        int value=-1;
        String userInput;
        do{
            try{
                userInput = scanner.nextLine();
                checkForCommand(userInput);
                value = Integer.parseInt(userInput);
            }catch (Exception e){
                //System.out.println(ErrorMessage);
            }
            if(value<min || value>max){
                System.out.println(ErrorMessage);
                value=-1;
            }
        }while(value==-1 && !EndGameFlag);
        return value;
    }

    private boolean checkForCommand(String userInput) throws RemoteException {
        if(userInput.equals("/quit")){
            System.out.println("Quit command sent to the server");
            serverRMI.quitGame(playerNickname);
            return true;
        }else if(userInput.startsWith("/chat ")){
            System.out.println("Chat message sent");
            /*String sender, text, receiver;
            int atIndex = userInput.indexOf('@');
            receiver = userInput.substring(atIndex+1);
            atIndex = receiver.indexOf(' ');
            text = receiver.substring(atIndex+1);
            receiver = receiver.substring(0, atIndex);
            //System.out.println(receiver);
            //System.out.println(text);
             */
            String text = "", receiver = "";
            int atIndex = userInput.indexOf('@');
            if(atIndex!=-1) {
                receiver = userInput.substring(atIndex + 1);
                atIndex = receiver.indexOf(' ');
                text = receiver.substring(atIndex + 1);
                receiver = receiver.substring(0, atIndex);
            }
            else {
                receiver = "all";
                atIndex = userInput.indexOf(' ');
                text = userInput.substring(atIndex + 1);
            }
            serverRMI.chatMessage(playerNickname, text, receiver);
            return true;
        }else{
            return false;
        }
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


            if(connectToRMIserver(null))
                System.out.println("Connected to the server");

            Scanner userInput = new Scanner(System.in);
            boolean connected;


            System.out.println("Enter (1) if you want to join a game, (2) if you are trying to reconnect to a game");
            int typeOfConnection = checkedInputForIntValues(userInput, 1, 2,"Insert (1) or (2)!");

            System.out.println("Now insert the nickname for the game: ");
            int returnCode;
            String nickname;

            if(typeOfConnection==1){
                //join the game
                joinGame(userInput);
                if(!serverRMI.hasGameStarted())
                    System.out.println("waiting for players...");
            }
            else{
                //reconnect to the game
                reconnectToGame(userInput);
            }

            //wait for the game to start
            while(!GameStartFlag){
                try{
                    Thread.sleep(500);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            //the game starts!
            System.out.println("the game actually started");
            //we continue to play until the game is over
            while(!EndGameFlag){
                //we do stuff only when it's our turn
                Thread.sleep(500);
                System.out.println("While it is not your turn you can chat with other players or quit the game");
                System.out.println("To quit enter /quit, to chat with others enter /chat @playerNickname your_message");
                while(!MyTurnFlag){
                    String input = userInput.nextLine();
                    if(!checkForCommand(input))
                        System.out.println("Not a valid command!");
                }


                while(MyTurnFlag){
                    userInput = new Scanner(System.in);
                    waitForNotifications();
                    rearrangedTiles = new ArrayList<>();
                    drawnTiles = new ArrayList<>();

                    printStartOfTurn();

                    System.out.println("Here is the board: ");
                    printBoard(serverRMI.getBoard());

                    //Draw tiles from board
                    do{
                        //read value for the row
                        System.out.println("Select the row from which to draw from:");
                        x = checkedInputForIntValues(userInput, 0, 8, "Insert a valid value for the row (0, 1, 2, ..., 8)");

                        //read value for the column
                        System.out.println("Select the column from which to draw from:");
                        y = checkedInputForIntValues(userInput, 0, 8, "Insert a valid value for the column (0, 1, 2, ..., 8)");

                        //read the value for the amount
                        System.out.println("How many tiles do you want to draw?");
                        amount = checkedInputForIntValues(userInput, 1, 3, "Insert a valid value for the amount (1, 2, 3)");

                        //read the value for the direction
                        System.out.println("In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                        directionInput = checkedInputForIntValues(userInput, 0, 3, "Insert a valid value for the direction (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                        switch(directionInput){
                            case 0: direction = Board.Direction.UP;break;
                            case 1: direction = Board.Direction.DOWN;break;
                            case 2: direction = Board.Direction.RIGHT;break;
                            case 3: direction = Board.Direction.LEFT;break;
                        }

                        //now we try to draw the tiles
                        drawnTiles = serverRMI.drawTilesFromBoard(playerNickname,x,y,amount,direction);
                        if(drawnTiles !=null){
                            //the tiles are correctly drawn
                            correctlyDrawn =true;
                        }
                        else {
                            //the tiles we have chosen cannot be drawn
                            System.out.println("You cannot draw those Tiles, try again!");
                            correctlyDrawn=false;
                        }
                    }while(!correctlyDrawn && !EndGameFlag);

                    waitForNotifications();
                    System.out.println("Here are your tiles");
                    for(int i=0;i<drawnTiles.size();i++){
                        System.out.print((i+1)+") "+drawnTiles.get(i).getType()+"  ");
                    }
                    System.out.println();
                    System.out.println("Here is your Shelf: ");
                    printShelf(serverRMI.getMyShelf(playerNickname));

                    do{
                        alreadyInsertedTiles = new ArrayList<Integer>();

                        //read the value for the column
                        System.out.println("Choose in which column you want to insert the tiles: ");
                        column = checkedInputForIntValues(userInput, 1, 5, "Insert a valid value for the column (1, 2, ..., 5)");

                        //rearrange the drawn tiles
                        System.out.println("Now choose in which order you want to insert the tiles");
                        for(int i=0;i<drawnTiles.size();i++){
                            do{
                                System.out.println("Select the next tile to insert in the column: ");
                                tileToBeRearranged=checkedInputForIntValues(userInput, 1, amount, "Insert a number between 1 and "+amount);

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
                        //inser the tiles in the shelf
                        correctlyInserted= serverRMI.insertTilesInShelf(playerNickname,rearrangedTiles,column-1);

                    }while(!correctlyInserted && !EndGameFlag);
                    waitForNotifications();

                    System.out.println("Shelf at the end of the turn: ");
                    printShelf(serverRMI.getMyShelf(playerNickname));
                    waitForNotifications();
                    setMyTurnFlag(false);
                    System.out.println("Your turn has ended, you now have "+serverRMI.getPoints(playerNickname)+" pts");
                }
            }
        }catch(RemoteException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void printShelf(Tile[][] grid){
        System.out.println("1 2 3 4 5");
        for(int i = 5;i>=0;i++) {
            for (int j = 0; j < 5; j++) {
                if (grid[i][j]==null) System.out.print("x ");
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

    private void printBoard(TilePlacingSpot[][] grid){
        System.out.println("  0 1 2 3 4 5 6 7 8");
        for(int i = 0;i<9;i++) {
            System.out.print(i+" ");
            for (int j = 0; j < 9; j++) {
                if (!grid[i][j].isAvailable()) System.out.print("x ");
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

    private void waitForNotifications(){
        try{
            Thread.sleep(300);
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void printStartOfTurn() throws RemoteException {
        System.out.println("*********  " + playerNickname + ": your turn  *********");

        Tile[][] shelf = serverRMI.getMyShelf(playerNickname);
        Tile[][] pgCard = serverRMI.getMyPersonalGoal(playerNickname);

        System.out.println("Your shelf:      Your Personal goal:");
        for(int row=5;row>=0;row--){
            for(int column =0;column<5;column++) {
                if (shelf[row][column]==null) System.out.print("x ");
                else{
                    switch(shelf[row][column].getType()){
                        case CAT: System.out.print("C ");break;
                        case BOOK:System.out.print("B ");break;
                        case GAME:System.out.print("G ");break;
                        case FRAME:System.out.print("F ");break;
                        case PLANT:System.out.print("P ");break;
                        case TROPHY:System.out.print("T ");break;
                    }
                }
            }
            System.out.print("          ");
            for(int column =0;column<5;column++) {
                if (pgCard[row][column]==null) System.out.print("x ");
                else{
                    switch(pgCard[row][column].getType()){
                        case CAT: System.out.print("C ");break;
                        case BOOK:System.out.print("B ");break;
                        case GAME:System.out.print("G ");break;
                        case FRAME:System.out.print("F ");break;
                        case PLANT:System.out.print("P ");break;
                        case TROPHY:System.out.print("T ");break;
                    }
                }
            }
            System.out.println("");
        }
        System.out.println("");
        System.out.println("Here are the common goals: ");
        System.out.println(serverRMI.getCommonGoalCardDescription());


    }

}
