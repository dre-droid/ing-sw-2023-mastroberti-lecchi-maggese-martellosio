package Server.Socket;

import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import com.google.gson.Gson;
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
    private static List<Player> leaderboard;
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
                    line = reader.readLine();
                    if (line.startsWith("[GSONBOARD]")){
                        line.replace("[GSONBOARD]", "");
                        Gson gson = new Gson();
                        board = gson.fromJson(line, Board.class);
                    }
                    if (line.startsWith("[GSONSHELF]")){
                        line.replace("[GSONSHELF]", "");
                        Gson gson = new Gson();
                        shelf = gson.fromJson(line, Shelf.class);
                    }
                    if (line.startsWith("[GSONLEAD]")){
                        line.replace("[GSONLEAD]", "");
                        Gson gson = new Gson();
                        leaderboard = gson.fromJson(line, List.class);
                    }

                    System.out.println("********* Turn n." + " - " + isPlaying.getNickname() + " is playing." + "*********");

                    if(line.startsWith("[REQUEST]")){
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




}


