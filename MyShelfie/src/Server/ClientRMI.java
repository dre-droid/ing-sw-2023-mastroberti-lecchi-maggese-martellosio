package Server;

import com.beust.ah.A;
import main.java.it.polimi.ingsw.Model.*;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientRMI {

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
                if (grid[i][j].isAvailable() == false) System.out.print("X ");
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


    public static void main(String[] args){
        try{
            int x,y,amount,directionInput,column,tileToBeRearranged;
            Board.Direction direction=null;
            List<Tile> drawnTiles=null;
            boolean correctlyDrawn=false;
            boolean correctlyInserted=false;
            List<Tile> rearrangedTiles=null;
            Registry registryServer = LocateRegistry.getRegistry();
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
            System.out.println("waiting for players...");
            while(!serverRMI.hasGameStarted()){}


            //the game starts!

            do{
                while(serverRMI.isMyTurn(nickname)){
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
                        System.out.println("Choose in which column you want to insert the tiles: ");
                        column = Integer.parseInt(userInput.nextLine());
                        System.out.println("Now choose in which order you want to insert the tiles");
                        for(int i=0;i<drawnTiles.size();i++){
                            System.out.println("Select the next to insert in the column: ");
                            tileToBeRearranged = Integer.parseInt(userInput.nextLine());
                            rearrangedTiles.add(0,drawnTiles.get(tileToBeRearranged-1));
                        }
                        correctlyInserted= serverRMI.insertTilesInShelf(nickname,rearrangedTiles,column-1);
                    }while(correctlyInserted);
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



        }catch(RemoteException | NotBoundException  e){
            e.printStackTrace();
        }

    }
}
