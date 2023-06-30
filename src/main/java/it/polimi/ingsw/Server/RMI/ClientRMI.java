package main.java.it.polimi.ingsw.Server.RMI;

import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.Player;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.TilePlacingSpot;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ClientRMI implements Runnable{

    boolean connectionProblem;
    private RMIinterface serverRMI;
    private ClientNotificationRMI notifications;
    private String playerNickname;

    private boolean MyTurnFlag;
    private boolean EndGameFlag;
    private boolean GameStartFlag;
    private boolean gameHasBeenCreated;
    int myport;
    int joinGameOutcome;

    public ClientRMI(){
        startClientNotificationServer();
        MyTurnFlag = false;
        EndGameFlag = false;
        GameStartFlag = false;
        gameHasBeenCreated = false;
        joinGameOutcome = -5;
    }

    public void setMyTurnFlag(boolean value){
        MyTurnFlag = value;
    }

    public void setGameHasBeenCreated(boolean value) {
        this.gameHasBeenCreated = value;
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

    private String myIp;


    /**
     * this method is used to connect to the rmi server
     * @param url ip address of the rmi server
     * @return
     */
    private boolean connectToRMIserver(String url){
        try{
            Registry registryServer = LocateRegistry.getRegistry(url);
            serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");
            serverRMI.ping();
        }catch(RemoteException | NotBoundException e){
            return false;
        }
        return true;
    }

    /**
     * this method is used to start the client notification server so that the server can send message to the
     * client
     */
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

    /**
     * this method is used to join the game on the server, it asks the client for a nickname and the tries to join
     * the lobby if the game has not started, if the game has already started then it asks the client the nickname
     * so he can try to reconnect to the game
     * @param userInput the scanner on which the client input data
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void joinGame(Scanner userInput) throws RemoteException, InterruptedException {
        boolean reconnected = false;
        do{
            System.out.println("Insert your nickname:");
            playerNickname = getPlayerNickname(userInput);

            //ip address
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                myIp = inetAddress.getHostAddress();
                //System.out.println("my ip is = "+myIp);
            } catch (UnknownHostException e) {
                System.out.println("cannot get ip address ");
            }

            if(serverRMI.hasGameStarted()){
                System.out.println("Trying to reconnect...");
                while(!serverRMI.reconnect(playerNickname,myport,myIp)){
                    System.out.println("Your nickname does not correspond to the one of the players in the game, insert your nickname to try again:");
                    playerNickname = getPlayerNickname(userInput);
                }
                System.out.println("Reconnected to the game! Now wait for your turn");
                reconnected = true;
                setGameStartFlag(true);


                periodicPing();
            }else{
                while(serverRMI.joinLobby(playerNickname, myport, myIp) != 0)
                    playerNickname = getPlayerNickname(userInput);
                periodicPing();
                if(serverRMI.isGameBeingCreated() && !serverRMI.firstInLobby(playerNickname)){
                    System.out.println("Game is being created by another player...");
                }

                synchronized (notifications){
                    while (joinGameOutcome == -5)
                        notifications.wait();
                }
                switch(joinGameOutcome){
                    case 0:{
                        //System.out.println("waiting for players...");
                    }break;
                    case -1: {
                        System.out.println("Creating a new game...How many players can join your game? (2, 3, 4)");
                        int numPlayers = checkedInputForIntValues(userInput,2,4,"Insert a valid value for the number of players (2, 3, 4)");

                        if(serverRMI.createNewGame(playerNickname,numPlayers,myport,myIp)){
                            joinGameOutcome=0;
                        }
                        else{
                            System.out.println("Somebody already created the game, try to join again, insert your nickname");
                        }
                    }break;
                    case -2:{
                        System.out.println("Try again later");
                    }break;
                    case -3:{ //should never reach
                        System.out.println("Try a different nickname");
                    }break;
                    case -4:{
                        System.out.println("The game has already started and the nickname you provided does not correspond to a player that have to reconnect");
                    }
                }
            }
        }while(joinGameOutcome!=0 && !reconnected);
    }

    private void reconnectToGame(Scanner userInput){
        System.out.println("Enter the nickname you used last time to join the game");
        playerNickname = userInput.nextLine();
        boolean reconnected = false;
        do{
            try{
                reconnected= serverRMI.reconnect(playerNickname, myport,myIp);
            }catch(RemoteException e){
            }

            if(!reconnected){
                System.out.println("Cannot reconnect to the game, someone is already connected with this nickname, try with another one");
                playerNickname = userInput.nextLine();
            }
        }while(!reconnected);
        System.out.println("You reconnected to the game, wait for your turn now");
    }

    /**
     * this method is used to check int values that the user input so that he does not insert problematic values
     * @param scanner input scanner on which the player input data
     * @param min minimum int value
     * @param max maximum int value
     * @param ErrorMessage error message to print if the player type a wrong value
     * @return
     */
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

    /**
     * this method is used to check if the input of the client contains a command to send to the server
     * @param userInput string typed by the client
     * @return true if the string contained a command, false otherwise
     * @throws IOException
     */
    private boolean checkForCommand(String userInput) throws IOException {
        serverRMI.ping();
        if(userInput.equals("/quit")){
            System.out.println("Quit command sent to the server");
            serverRMI.quitGame(playerNickname);
            System.exit(0);
            return true;
        }else if(userInput.startsWith("/chat ")){

            String text = "", receiver = "";
            int atIndex;
            if(userInput.startsWith("/chat @")) {
                atIndex = userInput.indexOf('@');
                receiver = userInput.substring(atIndex + 1);
                atIndex = receiver.indexOf(' ');
                text = receiver.substring(atIndex + 1);
                receiver = receiver.substring(0, atIndex);
                if(!Objects.equals(receiver, playerNickname))
                    serverRMI.chatMessage(playerNickname, text, receiver, true);
            }
            else {
                receiver = "all";
                atIndex = userInput.indexOf(' ');
                text = userInput.substring(atIndex + 1);
                serverRMI.chatMessage(playerNickname, text, receiver, false);
            }

            return true;
        }else{
            return false;
        }
    }

    /**
     * this method is used to reset all the flags
     */
    public void resetFlags(){
        setGameStartFlag(false);
        setMyTurnFlag(false);
        setGameHasBeenCreated(true);
        setEndGameFlag(false);
    }

    /**
     * this method is used to connect to server and play
     */
    @Override
    public void run() {

        do{
            connectionProblem = false;
            resetFlags();
            try{
                int x,y,amount,directionInput,column,tileToBeRearranged;
                Board.Direction direction=null;
                List<Tile> drawnTiles=null;
                List<Integer> alreadyInsertedTiles;
                boolean correctlyDrawn=false;
                boolean correctlyInserted=false;
                boolean correctlyRearranged = false;
                List<Tile> rearrangedTiles=null;

                Scanner userInput = new Scanner(System.in);
                boolean connected;
                String serverIp;
                System.out.println("First of all insert the ip address of the server:");
                do {
                    serverIp = userInput.nextLine();
                    try{
                        InetAddress inetAddress = InetAddress.getByName(serverIp);
                        if(inetAddress instanceof Inet4Address){
                            if(serverIp.equals(inetAddress.getHostAddress())){
                                if(serverIp.equals("127.0.0.2")){
                                    connected = false;
                                }
                                else{
                                    //System.out.println("correct ip: "+serverIp);
                                    connected = connectToRMIserver(inetAddress.getHostAddress());
                                }
                            }
                            else{
                                connected = false;
                            }
                        }
                        else{
                            connected=false;
                        }
                    }catch(UnknownHostException uhe){
                        connected = false;
                    }
                    if(!connected){
                        System.out.println("The ip you used is not a correct ip or it does not correspond to the server ip");
                    }
                }while(!connected);

                System.out.println("Connected to the server");
                //System.out.println("correct ip: "+serverIp);
                //join the game
                joinGame(userInput);
                if(!serverRMI.hasGameStarted())
                    System.out.println("waiting for players...");

                //wait for the game to start
                synchronized (notifications){
                    while(!GameStartFlag)
                        notifications.wait();
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
                            if(!input.isEmpty())
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
                            if(amount==1){
                                direction = Board.Direction.RIGHT;
                            }else{
                                System.out.println("In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                                directionInput = checkedInputForIntValues(userInput, 0, 3, "Insert a valid value for the direction (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                                switch(directionInput){
                                    case 0: direction = Board.Direction.UP;break;
                                    case 1: direction = Board.Direction.DOWN;break;
                                    case 2: direction = Board.Direction.RIGHT;break;
                                    case 3: direction = Board.Direction.LEFT;break;
                                }
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
                            alreadyInsertedTiles = new ArrayList<>();

                            //read the value for the column
                            System.out.println("Choose in which column you want to insert the tiles: ");
                            column = checkedInputForIntValues(userInput, 1, 5, "Insert a valid value for the column (1, 2, ..., 5)");

                            //rearrange the drawn tiles
                            System.out.println("Now choose in which order you want to insert the tiles");
                            if(drawnTiles.size()==1){
                                rearrangedTiles = drawnTiles;
                            }else{
                                for(int i=0;i<drawnTiles.size();i++){
                                    do{
                                        System.out.println("Select the next tile to insert in the column: ");
                                        tileToBeRearranged=checkedInputForIntValues(userInput, 1, amount, "Insert a number between 1 and "+amount);

                                        int finalTileToBeRearranged = tileToBeRearranged;
                                        if(alreadyInsertedTiles.stream().noneMatch((num)->(num== finalTileToBeRearranged))){
                                            alreadyInsertedTiles.add(tileToBeRearranged);
                                            rearrangedTiles.add(drawnTiles.get(tileToBeRearranged-1));
                                            correctlyRearranged=true;
                                        }else{
                                            System.out.println("You already inserted this tile, try again");
                                        }
                                    }while(!correctlyRearranged);
                                    correctlyRearranged=false;
                                }
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
            } catch(InterruptedException | IOException e){
                System.out.println("Problem in the connection to the server, try to reconnect...");
                e.printStackTrace();
                connectionProblem = true;
            }
        }while (connectionProblem && !getEndGameFlag());

    }

    /**
     * this method is used to print the shelf on the command line
     * @param grid matrix of tile representing the shelf to print
     */
    private void printShelf(Tile[][] grid){
        System.out.println("1 2 3 4 5");
        for(int i = 0; i <6 ; i++) {
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

    /**
     * this method is used to print the board on the command line
     * @param grid matrix of tile placing spot representing the board to print
     */
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

    /**
     * this method is used to print all the information at the beginning of a turn
     * @throws RemoteException
     */
    private void printStartOfTurn() throws RemoteException {
        System.out.println("*********  " + playerNickname + ": your turn  *********");

        Tile[][] shelf = serverRMI.getMyShelf(playerNickname);
        Tile[][] pgCard = serverRMI.getMyPersonalGoal(playerNickname);
        for(Player p : serverRMI.getLeaderboard()){
            if(p.hasEndGameToken()){
                System.out.println("CAREFULF THIS IS YOUR LAST TURN:" + " " + p.getNickname()+" " + "HAS GOT THE ENDGAME TOKEN");
            }
        }

        System.out.println("Your shelf:      Your Personal goal:");
        for(int row=0;row<6;row++){
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

        //Printing other players Shelfs
        for(Player p: serverRMI.getLeaderboard()){
            if(!p.getNickname().equals(playerNickname)){
                System.out.println(p.getNickname()+"'s Shelf");
                printShelf(serverRMI.getMyShelf(p.getNickname()));
            }
        }
    }

    /**
     * this method is used to ping serverRMI
     * @author Diego Lecchi
     */
    public void periodicPing() throws RemoteException {
        new Thread(() -> {
            while (true) {
                try {
                    serverRMI.setLastPing(playerNickname);
                } catch (RemoteException e) {
                    connectionProblem = true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    connectionProblem = true;
                }
            }
        }).start();
    }

    /**
     * this method is used to let the client insert his nickname according to the rules
     * @param userInput scanner on which the client types
     * @return
     */
    public String getPlayerNickname(Scanner userInput){
        String nickname =  userInput.nextLine();
        while (nickname.length() > 15 || nickname.equals("") || nickname.contains("@") || nickname.contains(" ") ||
                nickname.startsWith("/") || nickname.equals("Server")){
            System.out.println("Invalid nickname. Try again:");
            nickname = userInput.nextLine();
        }
        return nickname;
    }

}
