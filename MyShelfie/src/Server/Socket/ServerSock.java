package Server.Socket;


import com.google.gson.Gson;
import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.Player;
import main.java.it.polimi.ingsw.Model.Shelf;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

public class ServerSock {

    private final ArrayList<socketClient> clients = new ArrayList<>();
    private Server.Controller controller;
    //private static final ArrayList<Game> game = new ArrayList<>();


    public void setController(Server.Controller controller){
        this.controller = controller;
    }
    public void runServer(){
        new Thread(() -> {
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(59010);

                System.out.println("Waiting for client connection...");
                while (true){
                    try {
                        Socket client = serverSocket.accept();
                        acceptClient(client);

                    }
                    catch (IOException e) {
                        System.out.println("Ded");  //lol
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
        System.out.println("STEETETETT");
    }

    /**
     * Creates thread to let a client join the game (thread allows multiple connections simultaneously). Adds client's socket to clients list if successful
     * @param client
     */
    private void acceptClient(Socket client) {
        Runnable acceptClient = () -> {
            try {
                boolean repeat = true;
                while (repeat){
                    int resultValue = playerJoin(client);

                    if (resultValue == -3) repeat = true;   //invalid nickname
                    else if (resultValue == 0|| resultValue == -1) {    //successfully joined
                        repeat = false;
                    }
                };    //while nickname is not valid, keep trying for a new name
            } catch (IOException e) {
                try {client.close();}
                catch (IOException ex) {ex.printStackTrace();}
                finally {System.out.println("Client failed to join");}
            }
        };

        Thread thread = new Thread(acceptClient);
        thread.start();
    }

    /**
     * Lets player pick a nickname and - if first to join - create a new game
     * @param client
     * @throws IOException
     */
    private int playerJoin(Socket client) throws IOException {
        String nickname;
        /*
        for (Game g:game) {
            if(g.getPlayerList().size()<g.getNumOfPlayers()){
                System.out.println("fai tutte cose");
                return;
            }
        }
         */
        InputStream input = client.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        //asks player nickname
        out.println("[REQUEST] Inserisci un nickname:");
        nickname = reader.readLine();
        out.println("Il nickname inserito è:  "+ nickname);


        switch (controller.joinGame(nickname)) {
            //no existing game
            case -1 -> {
                out.println("[REQUEST] Inserisci il numero di giocatori: ");
                String line = reader.readLine();
                System.out.println(line);
                controller.createNewGame(nickname, Integer.parseInt(line));     //create new game
                out.println("Il numero di giocatori inserito è:  " + line);
                return -1;
            }

            //game has started
            case -2 -> {
                return -2;
            }

            //name in use
            case -3 -> {
                return -3;
            }

            //successful
            case 0 -> {
                clients.add(new socketClient(client, nickname));
                return 0;
            }
        }
        return -4;  //should never reach!
    }

    /**
     * Queries the client for info on his turn's drawn tiles
     * @param nickname
     * @param b
     * @param shelf
     * @return
     */
    public drawInfo drawInquiry(String nickname, Board b, Shelf shelf, List<Player> leaderboard){
        Socket playerSocket = null;
        drawInfo drawInfo = new drawInfo();

        for (socketClient c: clients)
            if (c.getName().equals(nickname)) playerSocket = c.getSocket();

        try {
            InputStream input = playerSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);

            Gson gson = new Gson();

            out.println("[NICKNAME]" + nickname);

            String jsonBoard = gson.toJson(b);
            out.println("[GSONBOARD]" + jsonBoard);

            String jsonShelf = gson.toJson(shelf);
            out.println("[GSONBOARD]" + jsonBoard);

            String jsonLeaderboard = gson.toJson(leaderboard);
            out.println("[GSONLEAD]" + jsonBoard);

            out.println("[REQUEST] Pesca tessere dalla tavola: (x, y, amount, direction) - direction [UP:0, DOWN:1, LEFT:2, RIGHT:3]");
            String line = reader.readLine();
            Integer.parseInt(line.replaceAll("[\\D]", "")); //replaces all non digits to blanks
            Scanner scanner = new Scanner(line);

            for (int i = 0; i < 4; i++){
                switch(i){
                    case 0: drawInfo.setX(scanner.nextInt());
                    case 1: drawInfo.setY(scanner.nextInt());
                    case 2: drawInfo.setAmount(scanner.nextInt());
                    case 3: drawInfo.setDirection(Board.Direction.values()[scanner.nextInt()]);
                }
            }

            out.println("[REQUEST] Inserisci la colonna della shelf in cui inserire le tessere pescate: [0 ... 4]");
            line = reader.readLine();
            drawInfo.setColumn(Integer.parseInt(line));

            System.out.println(drawInfo.getX());
            System.out.println(drawInfo.getY());
            System.out.println(drawInfo.getDirection());
            System.out.println(drawInfo.getAmount());
            System.out.println(drawInfo.getColumn());
            return drawInfo;
        } catch(Exception e){}

        return drawInfo;
    }

}