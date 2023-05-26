package Server.Socket;

import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import main.java.it.polimi.ingsw.Model.Board;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.StrategyCommonGoal;
import main.java.it.polimi.ingsw.Model.PersonalGoalCard;
import main.java.it.polimi.ingsw.Model.Player;
import main.java.it.polimi.ingsw.Model.Shelf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClientSocket {
    private Board board = null;
    private Shelf shelf = null;
    private PersonalGoalCard personalGoalCard = null;
    private List<CommonGoalCard> commonGoalCards = null;
    private List<Player> leaderboard = null;
    private String nickname = null;
    private Socket socket = null;
    private Map<Integer, PersonalGoalCard> pgcMap = null;

    public final Gson gson = new GsonBuilder().registerTypeAdapter(StrategyCommonGoal.class, new StrategyAdapter()).create();
    public String isPlaying = "";
    public String messageFromServer = "";
    public final Object object = new Object();
    public String nextScene = "";
    public String chatMessage = "";
    public String turnOfPlayer = "";
    public boolean turnHasEnded = false;

    //used by ClientWithChoice
    public void runServer(){
        try{
            //connect to server
            socket= new Socket("127.0.0.1",59010);
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
                    Thread.sleep(2000);
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
//                    synchronized (object) {
                        messageFromServer = line;
                        deserializeObjects(line);
                        handleServerRequest(line);
//                    }
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
        if (line.equals("[CONNECTED]")) {
            serverPinger();
        }
        if (line.startsWith("[YOUR TURN]")) {
            printTurn();
            System.out.println(line);
        }
        if (line.startsWith("[INVALID MOVE]")) {
            System.out.println("You cannot select those tiles. Try again.\n");
        }
        if (line.startsWith("[REQUEST]") || line.startsWith("[MESSAGE") || line.startsWith("[INFO]")) {
            System.out.println(line);
        }
        if (line.startsWith("[INFO]: Chosen nickname:")){
            nickname = line.replace("[INFO]: Chosen nickname: ", "");
        }
        if (line.startsWith("[SHELF]")) {
            System.out.println(line);
            printShelf(getShelf());
        }
        if (line.startsWith("[TURNEND]")) {
            printShelf(getShelf());
            System.out.println();
            System.out.println(line);
            System.out.println("******************************");
        }
        if (line.startsWith("[GAMEEND]")) {
            System.out.println(line);
            System.exit(0);
        }

        //GUI
        if (line.startsWith("[REQUEST] Invalid nickname.") || line.startsWith("[REQUEST]: Nickame already in use") || line.startsWith("[REQUEST]: Invalid input, you can choose between 2 and 4 players:")){
            line = line.replace("[REQUEST]", "");
            line = line.replace("[INFO]", "");
            nextScene = line.replace(":", "");
            System.out.println("client socket " + nextScene);
            notify();
        }
        else if (line.startsWith("[REQUEST]: Choose the number of players for the game:")){
            nextScene = "MatchType";
            notify();
        }
        else if (line.startsWith("[INFO]: Game is being created by another player") || line.startsWith("[INFO]: Waiting for all players to connect...")) {
            nextScene = "GameScene";
            notify();
        }
        else if (line.startsWith("[MESSAGE FROM")){
            chatMessage = line;
            notify();
        }
        //if(line.contains("Invalid input") && !line.startsWith("[MESSAGE_FROM")){
        //    chatMessage = line;
        //    notify();
        //}
        if (line.startsWith("[INFO]: Game is starting.")){
            nextScene = "GameStart";
            turnOfPlayer = line.replace("[INFO]: Game is starting. ", "");
            notify();
        }

    }

    /**
     * Handles deserializing objects sent from serverSocket. If [CURRENTPLAYER] message is recieved, client sets turnHasEnded to true.
     * @param line: the serialized object sent from the server
     */
    private synchronized void deserializeObjects(String line){
        if (line.startsWith("[GSONBOARD]")) {
            String gsonString = line.replace("[GSONBOARD]", "");
            board = gson.fromJson(gsonString, Board.class);
        }
        if (line.startsWith("[GSONSHELF]")) {
            String gsonString = line.replace("[GSONSHELF]", "");
            shelf = gson.fromJson(gsonString, Shelf.class);
        }
        if (line.startsWith("[GSONLEAD]")) {
            TypeToken<List<Player>> typeToken = new TypeToken<>() {};
            String gsonString = line.replace("[GSONLEAD]", "");
            leaderboard = gson.fromJson(gsonString, typeToken.getType());
        }
        if (line.startsWith("[GSONPGC]")) {
            TypeToken<PersonalGoalCard> typeToken = new TypeToken<>() {};
            String gsonString = line.replace("[GSONPGC]", "");
            personalGoalCard = gson.fromJson(gsonString, typeToken.getType());
        }
        if (line.startsWith("[GSONCGC]")) {
            line = line.replace("[GSONCGC]", "");
            TypeToken<List<CommonGoalCard>> typeToken = new TypeToken<>() {};
            commonGoalCards = gson.fromJson(line, typeToken.getType());
        }
        if (line.startsWith("[NICKNAME]")) {
            nickname = line.replace("[NICKNAME]", "");
        }
        if (line.startsWith("[GSONPGMAP]")){
            line = line.replace("[GSONPGMAP]", "");
            TypeToken<Map<Integer, PersonalGoalCard>> typeToken = new TypeToken<>() {};
            pgcMap = gson.fromJson(line, typeToken.getType());
        }
        if (line.startsWith("[CURRENTPLAYER]")){
            line = line.replace("[CURRENTPLAYER]", "");
            isPlaying = line;
            turnHasEnded = true;
        }
        notify();
    }

    /**
     * Prints a command line view of the player's turn
     */
    private void printTurn(){
        System.out.println();
        System.out.println("*********  " + nickname + ": your turn  *********");

        //shelf & personalGoal print
        Scanner scannerpg = new Scanner(personalGoalCard.toString());
        Scanner scannercg = new Scanner(commonGoalCards.get(0).getDescription() + "\n" + commonGoalCards.get(1).getDescription());

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
        for (Player p: leaderboard) {
            System.out.print(i + 1 + ". " + p.getNickname() + ": ");
            System.out.println(p.getScore());
            i++;
        }
        System.out.println();

        //print other player shelfs
        for(Player p: leaderboard){
            if(!p.getNickname().equals(getNickname())){
                System.out.println(p.getNickname()+"'s Shelf");
                printShelf(p.getShelf());

            }
        }
    }

    private void printShelf(Shelf shelf){
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
                        if (!message.startsWith("[GUI])")) output.println(message);
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
                PrintWriter output = new PrintWriter(socket.getOutputStream(), false);
                synchronized (this){     // writing to output is synchronized with other writing methods
                    output.println(message);
                    output.flush();
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }
    }

    //getters
    public Board getBoard() {
        return board;
    }
    public Shelf getShelf() {
        return shelf;
    }
    public PersonalGoalCard getPersonalGoalCard() {
        return personalGoalCard;
    }
    public List<CommonGoalCard> getCommonGoalCards() {
        return commonGoalCards;
    }
    public List<Player> getLeaderboard() {
        return leaderboard;
    }
    public String getNickname() {
        return nickname;
    }
    public Socket getSocket() {
        return socket;
    }
    public Map<Integer, PersonalGoalCard> getPgcMap() {
        return pgcMap;
    }
    public boolean isMyTurn() {return isPlaying.equals(nickname);}
}


