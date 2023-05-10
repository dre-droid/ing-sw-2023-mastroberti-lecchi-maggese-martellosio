package Server.Socket;

import java.lang.reflect.Type;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import com.google.gson.Gson;
import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Model.PersonalGoalCard;
import main.java.it.polimi.ingsw.Model.Player;
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
    public String messageFromServer = "";
    private Socket socket;

    //used by ClientWithChoice
    public void runServer(){
        try{
            //connect to server
            socket= new Socket("127.0.0.1", 59010);
            socket.setKeepAlive(true);
            //pinger(socket);

            try{
                serverListener(socket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {throw new RuntimeException(e);}
    }

    private void serverListener(Socket socket) {
        Runnable serverListener = () -> {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line = null;
                boolean active = true;
                clientSpeaker(socket);

                while(active){
                    Gson gson = new Gson();
                    line = reader.readLine();
                    if (line != null) messageFromServer = line;

                    // ************* DESERIALIZATION ****************
                    if (line.startsWith("[GSONBOARD]")){
                        String gsonString = line.replace("[GSONBOARD]", "");
                        board = gson.fromJson(gsonString, Board.class);
                    }
                    if (line.startsWith("[GSONSHELF]")){
                        String gsonString = line.replace("[GSONSHELF]", "");
                        shelf = gson.fromJson(gsonString, Shelf.class);
                    }
                    if (line.startsWith("[GSONLEAD]")){
                        String gsonString = line.replace("[GSONLEAD]", "");
                        leaderboard = gson.fromJson(gsonString, List.class);
                    }
                    if (line.startsWith("[GSONPGC]")){
                        String gsonString = line.replace("[GSONPGC]", "");
                        personalGoalCard = gson.fromJson(gsonString, String.class);
                    }
                    if (line.startsWith("[GSONCGC]")){
                        String gsonString = line.replace("[GSONCGC]", "");
                        commonGoalCard = gson.fromJson(gsonString, String.class);
                    }
                    if (line.startsWith("[NICKNAME]")){
                        String gsonString = line.replace("[NICKNAME]", "");
                        nickname = gsonString;
                    }
                    // ********************************************
                    if (line.startsWith("[YOUR TURN]")){
                        printTurn();
                        System.out.println(line);
                    }
                    if(line.startsWith("[INVALID MOVE]")){
                        System.out.println("Non puoi selezionare queste tessere. Riprova.\n");
                    }
                    if (line.startsWith("[INFO]") || line.startsWith("[REQUEST]")){
                        System.out.println(line);
                    }
                    if (line.startsWith("[SHELF]")){
                        System.out.println(line);
                        printShelf();
                    }
                    if (line.startsWith("[TURNEND]")){
                        printShelf();
                        System.out.println();
                        System.out.println(line);
                        System.out.println("******************************");
                    }
                    if (line.startsWith("[GAMEEND]")){
                        System.out.println(line);
                        System.exit(0);
                    }
                    if(line.startsWith("[MESSAGE")){
                        System.out.println(line);
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

    private void printTurn(){
        System.out.println();
        System.out.println("*********  " + nickname + ": your turn  *********");

        //shelf & personalGoal print
        Scanner scannerpg = new Scanner(personalGoalCard);
        Scanner scannercg = new Scanner(commonGoalCard);

        System.out.println("*** Shelf ***  *** Personal Goal Card ***  *** Common Goal Card ***");
        for (int i = 5; i >= 0; i--) {
            System.out.print("   ");
            for (int j = 0; j < 5; j++){
                if (shelf.getGrid()[i][j] == null) System.out.printf("x ");
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
        for (int i = 5; i >= 0; i--) {
            for (int j = 0; j < 5; j++) {
                if (shelf.getGrid()[i][j] == null) System.out.printf("x ");
                else System.out.printf("%s ", shelf.getGrid()[i][j].toString());
            }
            System.out.println();
        }
        System.out.println("*************");
    }

    private void clientSpeaker(Socket socket){
        Runnable clientSpeaker = () ->{
            try{
                String message;

                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                boolean active = true;
                while(active){
                    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                    message = bufferRead.readLine();
                    output.println(message);
                }

            }catch (Exception e){
                throw new RuntimeException(e);
            }
        };
        new Thread(clientSpeaker).start();
    }

    /**
     * @param message prints message to the socket's output stream
     */
    public void clientSpeaker(String message){
            try{
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                output.println(message);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
    }
    /*
    private void pinger(Socket s){
        new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                out.println("[PING]");
                Thread.sleep(4000);

            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }
    */
}


