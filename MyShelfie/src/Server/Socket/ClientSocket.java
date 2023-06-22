package Server.Socket;

import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.StrategyCommonGoal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public abstract class ClientSocket {
    protected Board board = null;
    protected Shelf shelf = null;
    protected PersonalGoalCard personalGoalCard = null;
    protected List<CommonGoalCard> commonGoalCards = null;
    protected List<Player> leaderboard = null;
    protected String nickname = null;
    protected Socket socket = null;
    protected Map<Integer, PersonalGoalCard> pgcMap = null;

    public final Gson gson = new GsonBuilder().registerTypeAdapter(StrategyCommonGoal.class, new StrategyAdapter()).create();
    public String isPlaying = "";
    public String messageFromServer = "";
    public String chatMessage = "";
    public boolean turnHasEnded = false;
    public String turnOfPlayer = "";
    public String nextScene = "";
    public List<Tile> drawnTiles;
    public long lastPing;
    public final long disconnectionTime = 10000;
    protected String ip = "127.0.0.1";

    public void runServer(){
        try{
            //connect to server
            socket= new Socket(ip,59010);
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
    protected void serverPinger(){
        Runnable serverPinger = () -> {
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                while (!socket.isClosed()) {
                    synchronized (this) {   //writing to output is synchronized with other writing methods
                        output.println("[PING]");
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        new Thread(serverPinger).start();
    }

    protected void serverListener() {
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
                    if(!line.equals("[PING]")) {
                        handleServerRequestCommon(line);
                        handleServerRequest(line);
                    }
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

    private synchronized void handleServerRequestCommon(String line){
        if (line.startsWith("[INFO] Chosen nickname:")){
            nickname = line.replace("[INFO] Chosen nickname: ", "");
        }
        if (line.startsWith("[ALLDISCONNECTED]")){
            System.out.println("All players disconnected. You win!");
            System.exit(0);
        }
        if (line.equals("[CONNECTED]")) {
            serverPinger();
            disconnectionCheck();
        }
    }

    /**
     * Handles server's received messages - implemented in the concrete children classes
     */
    protected synchronized void handleServerRequest(String line){}

    /**
     * Handles deserializing objects sent from serverSocket. If [CURRENTPLAYER] message is recieved, client sets turnHasEnded to true.
     * @param line: the serialized object sent from the server
     */
    protected synchronized void deserializeObjects(String line){
        if (line.startsWith("[GSONBOARD]")) {
            System.out.println("Deserialized board.");
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
        if (line.equals("[PING]")){
            lastPing = System.currentTimeMillis();
        }
//        if (line.startsWith("[DRAWNTILES]")){
//            TypeToken<List<Tile>> typeToken = new TypeToken<>() {};
//            String gsonString = line.replace("[DRAWNTILES]", "");
//            drawnTiles = gson.fromJson(gsonString, typeToken.getType());
//        }
        notify();
    }

    /**
     * Creates a thread that reads the terminal and sends each line to the serverSocket
     */
    public void clientSpeaker(){
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
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            synchronized (this){     // writing to output is synchronized with other writing methods
                out.println(message);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public void disconnectionCheck(){
        new Thread(() -> {
            lastPing = System.currentTimeMillis();
            while(true){
                if(System.currentTimeMillis() - lastPing > disconnectionTime) {
                    disconnectionAlert();
                    break;
                }
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    protected void disconnectionAlert(){}

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


