package Server.Socket;

import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.Shelf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClientSocket {
    private Board board;
    private Shelf shelf;
    private String personalGoalCard;
    private String commonGoalCard;
    private List<String> leaderboard;
    private String nickname;
    private Socket socket;
    private final Gson gson = new Gson();

    public String messageFromServer = "";
    public final Object object = new Object();
    public String nextScene = "";

    //used by ClientWithChoice
    public void runServer(){
        try{
            //connect to server
            socket= new Socket("127.0.0.1", 59010);
            socket.setKeepAlive(true);

            try{
                serverListener();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {throw new RuntimeException(e);}
    }

    /**
     * Creates a thread that sends a PING to the server every 5 seconds
     */
    private void serverPinger(){
        Runnable serverPinger = () -> {
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                while (!socket.isClosed()) {
                    synchronized (this) {   //writing to output is synchronized with other writing methods
                        output.println("[PING]");
                    }
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        new Thread(serverPinger).start();
    }

    private void serverListener() {
        Runnable serverListener = () -> {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                clientSpeaker();

                while(!socket.isClosed()) {
                    String line = reader.readLine();
                    synchronized (object) {
                        messageFromServer = line;
                        deserializeObjects(line);
                        handleServerRequest(line);
                    }
                    }
                }
            catch (SocketException e) {
                System.out.println("Socket closed.");
            }
            catch (IOException e) {e.printStackTrace();}
        };

        new Thread(serverListener).start();
    }

    /**
     * Handles server's received messages
     */
    private synchronized void handleServerRequest(String line){
        if (line.equals("[CONNECTED]"))
            serverPinger();
        if (line.startsWith("[YOUR TURN]")) {
            printTurn();
            System.out.println(line);
        }
        if (line.startsWith("[INVALID MOVE]")) {
            System.out.println("You cannot select those tiles. Try again.\n");
        }
        if (line.startsWith("[INFO]") || line.startsWith("[REQUEST]") || line.startsWith("[MESSAGE")) {
            System.out.println(line);
        }
        if (line.startsWith("[SHELF]")) {
            System.out.println(line);
            printShelf();
        }
        if (line.startsWith("[TURNEND]")) {
            printShelf();
            System.out.println();
            System.out.println(line);
            System.out.println("******************************");
        }
        if (line.startsWith("[GAMEEND]")) {
            System.out.println(line);
            System.exit(0);
        }
        //GUI
        if (line.startsWith("[REQUEST]: Choose the number of players for the game:")){
            nextScene = "MatchType";
            notify();
        }
        if (line.startsWith("[REQUEST] Invalid nickname. Try again.") || line.startsWith("[INFO]: Nickname in use, try another one:")){
            nextScene = "Unchanged";
            notify();
        }
        if (line.startsWith("[REQUEST]: Invalid input, you can choose between 2 and 4 players:")){
            nextScene = "Unchanged";
            notify();
        }
        if (line.startsWith("[INFO]: Game is starting")){
            nextScene = "GameScene";
            notify();
        }

    }

    /**
     * Handles deserializing objects sent from serverSocket
     * @param line: the serialized object sent from the server
     */
    private void deserializeObjects(String line){
        if (line.startsWith("[GSONBOARD]")) {
            String gsonString = line.replace("[GSONBOARD]", "");
            board = gson.fromJson(gsonString, Board.class);
        }
        if (line.startsWith("[GSONSHELF]")) {
            String gsonString = line.replace("[GSONSHELF]", "");
            shelf = gson.fromJson(gsonString, Shelf.class);
        }
        if (line.startsWith("[GSONLEAD]")) {
            String gsonString = line.replace("[GSONLEAD]", "");
            leaderboard = gson.fromJson(gsonString, List.class);
        }
        if (line.startsWith("[GSONPGC]")) {
            String gsonString = line.replace("[GSONPGC]", "");
            personalGoalCard = gson.fromJson(gsonString, String.class);
        }
        if (line.startsWith("[GSONCGC]")) {
            String gsonString = line.replace("[GSONCGC]", "");
            commonGoalCard = gson.fromJson(gsonString, String.class);
        }
        if (line.startsWith("[NICKNAME]")) {
            nickname = line.replace("[NICKNAME]", "");
        }
    }

    /**
     * Prints a command line view of the player's turn
     */
    private void printTurn(){
        System.out.println();
        System.out.println("*********  " + nickname + ": your turn  *********");

        //shelf & personalGoal print
        Scanner scannerpg = new Scanner(personalGoalCard);
        Scanner scannercg = new Scanner(commonGoalCard);

        System.out.println("*** Shelf ***  *** Personal Goal Card ***  *** Common Goal Card ***");
        for (int i = 0; i < 6; i++) {
            System.out.print("   ");
            for (int j = 0; j < 5; j++){
                if (shelf.getGrid()[i][j] == null) System.out.print("x ");
                else System.out.printf("%s ", shelf.getGrid()[i][j].toString());
            }
            System.out.print("   ");
            System.out.print(scannerpg.nextLine());
            System.out.print("   ");
            if (scannercg.hasNextLine()) System.out.print(scannercg.nextLine());
            System.out.println();
        }
        System.out.println();

        //board print
        board.printGridMap();
        System.out.println();

        //leaderboard print
        System.out.println("Leaderboard");
        int i = 0;
        for (String s: leaderboard) {
            System.out.println(i + 1 + ". " + s);
            i++;
        }
        System.out.println();
    }

    private void printShelf(){
        System.out.println("*** Shelf ***");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (shelf.getGrid()[i][j] == null) System.out.print("x ");
                else System.out.printf("%s ", shelf.getGrid()[i][j].toString());
            }
            System.out.println();
        }
        System.out.println("*************");
    }

    /**
     * Creates a thread that reads the terminal and sends each line to the serverSocket
     */
    private void clientSpeaker(){
        Runnable clientSpeaker = () ->{
            try{
                String message;

                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                while(!socket.isClosed()){
                    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                    message = bufferRead.readLine();
                    synchronized (this) {   //writing to output is synchronized with other writing methods
                        output.println(message);
                    }
                }

            }catch (Exception e){
                throw new RuntimeException(e);
            }
        };
        new Thread(clientSpeaker).start();
    }

    /**
     * Prints the message string to the server's InputStream
     */
    public void clientSpeaker(String message){
            try{
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                synchronized (this){     //writing to output is synchronized with other writing methods
                    output.println(message);
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }
    }
}


