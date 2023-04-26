package Server.Socket;

import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
    private static Board board;
    private static Shelf shelf;
    private static String personalGoalCard;
    private static String commonGoalCard;
    private static List<String> leaderboard;
    private static String nickname;
    public static void main(String[] args) throws IOException {
        //System.out.println("Insert the port number: ");
        //BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        //String s = bufferRead.readLine();

        try{
            //connect to server
            Socket socket= new Socket("127.0.0.1", 59010);
            socket.setKeepAlive(true);

            try{
                serverListener(socket);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {throw new RuntimeException(e);}

    }

    public void runServer(){
        try{
            //connect to server
            Socket socket= new Socket("127.0.0.1", 59010);
            socket.setKeepAlive(true);

            try{
                serverListener(socket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {throw new RuntimeException(e);}
    }


    private static void serverListener(Socket socket) {
        Runnable serverListener = () -> {
            try {
                InputStream input = socket.getInputStream();
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                String message;
                boolean active = true;
                clientSpeaker(socket);

                while(active){
                    Gson gson = new Gson();
                    line = reader.readLine();


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

                    if(line.startsWith("[REQUEST]")){
                        System.out.println(line);
                        //BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                        //message = bufferRead.readLine();
                       // output.println(message);
                    }
                    if(line.startsWith("[YOUR TURN]")){
                        printTurn();
                        System.out.println(line);
                        //BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                        //message = bufferRead.readLine();
                        //output.println(message);
                    }
                    if(line.startsWith("[INVALID MOVE]")){
                        System.out.println("Non puoi selezionare queste tessere. Riprova.\n");
                        //BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                        //message = bufferRead.readLine();
                        //output.println(message);
                    }
                    if (line.startsWith("[INFO]")){
                        System.out.println(line);
                    }
                    }
                }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        new Thread(serverListener).start();
    }

    private static void printTurn(){
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

        System.out.println("******************************\n");
    }

    private static void clientSpeaker(Socket socket){
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

}


