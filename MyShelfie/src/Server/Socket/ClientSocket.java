package Server.Socket;

import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.it.polimi.ingsw.Model.Board;
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


    private static void serverListener(Socket socket) {
        Runnable serverListener = () -> {
            try {
                InputStream input = socket.getInputStream();
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                String message;
                boolean active = true;

                while(active){
                    Gson gson = new Gson();
                    line = reader.readLine();
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
                    if (line.startsWith("[NICKNAME]")){
                        String gsonString = line.replace("[NICKNAME]", "");
                        nickname = gsonString;
                    }
                    if(line.startsWith("[REQUEST]")){
                        System.out.println(line);
                        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                        message = bufferRead.readLine();
                        output.println(message);
                        }
                    if(line.startsWith("[YOUR TURN]")){
                        printTurn();
                        System.out.println(line);
                        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                        message = bufferRead.readLine();
                        output.println(message);
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
        System.out.println("*********  " + nickname + ": your turn *********");
        board.printGridMap();
        System.out.println();

        for (int i = 5; i >= 0; i--) {
            for (int j = 0; j < 5; j++){
                if (shelf.getGrid()[i][j] == null) System.out.printf("X ");
                else System.out.printf("%s ", shelf.getGrid()[i][j].toString());
            }
            System.out.println();
        }
        System.out.println();

        System.out.println("Leaderboard");
        int i = 0;
        for (String s: leaderboard) {
            System.out.println(i + 1 + ". " + s);
            i++;
        }
        System.out.println();


        System.out.println("******************************\n");
    }

}


