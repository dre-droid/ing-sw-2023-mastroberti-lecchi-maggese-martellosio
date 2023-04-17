package Server.Socket;


import com.beust.ah.A;
import com.google.gson.Gson;
import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Model.PersonalGoalCard;
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
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ServerSock {

    private final ArrayList<socketNickStruct> clients = new ArrayList<>();
    private Server.Controller controller;

    public void setController(Server.Controller controller){
        this.controller = controller;
    }

    /**
     * Creates a thread to accept clients.
     */
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
    }

    /**
     * Creates thread to let a client join the game (thread allows multiple connections simultaneously). Adds client's socket to
     * List<socketNickStruct> clients if successful
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
     * Helper function for acceptClient. Lets client pick a nickname and - if first to join - create a new game
     * @param client
     * @throws IOException
     * @return result of controller.joinGame()
     */
    private int playerJoin(Socket client) throws IOException {
        String nickname;
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
                String line;
                int counter = 0;
                do {
                    if (counter > 0) out.println("[INFO]: Numero di giocatori per la partita non valido. Riprova.");
                    out.println("[REQUEST] Inserisci il numero di giocatori: ");
                    line = reader.readLine();
                    System.out.println("Number of players selected: " + line);
                    counter++;
                }while (Integer.parseInt(line) < 2 || Integer.parseInt(line) > 4);
                controller.createNewGame(nickname, Integer.parseInt(line)); //create new game
                out.println("[INFO]: Il numero di giocatori inserito è:  " + line);
                clients.add(new socketNickStruct(client, nickname));
                return -1;
            }
            //game has started
            case -2 -> {
                return -2;
            }
            //name in use
            case -3 -> {
                out.println("[INFO]: Nickname già in uso, scegline un altro.");
                return -3;
            }
            //successful
            case 0 -> {
                clients.add(new socketNickStruct(client, nickname));
                return 0;
            }
        }
        return -4;  //should never reach!
    }

    /**
     * Queries the client for info on his turn's drawn tiles
     * @param nickname - the nickname of the client to query
     * @param b - game's board
     * @param shelf - client's board
     * @return drawInfo, a struct containing which tiles are drawn and the column where they are to be placed in client's shelf
     */
    public drawInfo drawInquiry(String nickname, Board b, Shelf shelf, PersonalGoalCard pgc, List<CommonGoalCard> cgc, List<Player> leaderboard){
        Socket playerSocket = null;
        drawInfo drawInfo = new drawInfo();

        for (socketNickStruct c: clients)
            if (c.getName().equals(nickname)){
                playerSocket = c.getSocket();
            }

        try {
            InputStream input = playerSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);

            //*************** SERIALIZATION
            Gson gson = new Gson();

            out.println("[NICKNAME]" + nickname);

            String jsonBoard = gson.toJson(b);
            out.println("[GSONBOARD]" + jsonBoard);

            String jsonShelf = gson.toJson(shelf);
            out.println("[GSONSHELF]" + jsonShelf);

            String jsonPersonalGoal = gson.toJson(pgc.toString());
            out.println("[GSONPGC]" + jsonPersonalGoal);

            //String jsonCommonGoal = gson.toJson(cgc.toString());
            //out.println("[GSONCGC]" + jsonCommonGoal);

            ArrayList<String> stringLeaderboard = new ArrayList<String>();
            for (Player p: leaderboard) stringLeaderboard.add(p.getNickname() + ": " + p.getScore());
            String jsonLeaderboard = gson.toJson(stringLeaderboard);
            out.println("[GSONLEAD]" + jsonLeaderboard);
            //***************

            out.println("[YOUR TURN] Pesca tessere dalla tavola: (x, y, amount, direction) - direction [UP:0, DOWN:1, RIGHT:2, LEFT:3]");
            String line = reader.readLine();
            line = line.replace(",", ""); //replaces all non digits to blanks
            line = line.replace("  ", " "); //replaces all multiple blanks to single blanks
            String[] numsArray = line.split( " ");

            drawInfo.setX(Integer.parseInt(numsArray[0]));
            drawInfo.setY(Integer.parseInt(numsArray[1]));
            drawInfo.setAmount(Integer.parseInt(numsArray[2]));
            drawInfo.setDirection(Board.Direction.values()[Integer.parseInt(numsArray[3])]);

            out.println("[REQUEST] Inserisci la colonna della shelf in cui inserire le tessere pescate: [0 ... 4]");
            line = reader.readLine();
            drawInfo.setColumn(Integer.parseInt(line));

            return drawInfo;
        } catch(Exception e){
            e.printStackTrace();
        }

        return drawInfo;
    }


    public void printErrorToClient(String message, String nickname) throws IOException{
        for (socketNickStruct s: clients)
            if (s.getName().equals(nickname)) {
                PrintWriter out = new PrintWriter(s.getSocket().getOutputStream(), true);
                out.println("[INVALID MOVE]" + message);
            }
    }

}