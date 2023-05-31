package Server.Socket;


import GUI.PositionStuff.Position;
import Server.Controller;
import Server.Server;
import com.beust.ah.A;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.StrategyCommonGoal;
import org.testng.internal.protocols.Input;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class ServerSock {

    private ArrayList<socketNickStruct> clients = new ArrayList<>();
    private Controller controller;
    private final Server server;
    private final long DISCONNECTION_TIME = 30000;  //disconnection threshold: 30s
    private final Gson gson = new GsonBuilder().registerTypeAdapter(StrategyCommonGoal.class, new StrategyAdapter()).create();
    public List<String> messageBuffer = new ArrayList<>();

    public ServerSock(Controller controller, Server server){
        this.controller = controller;
        this.server = server;
    }

    /**
     * Creates a thread to accept clients.
     */
    public void runServer(){
            System.out.println("Socket server up and running...");
            Thread runServer = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(59010)) {
                    checkForDisconnections();
                    while (true) {
                        Socket client = serverSocket.accept();
                        acceptClient(client);
                    }
                }catch (IOException e) {e.printStackTrace();}
            });
            runServer.start();
    }

    /**
     * Creates thread to let a client join the game (thread allows multiple connections simultaneously). Adds client's socket to
     * List<socketNickStruct> clients if successful. Also creates a clientListener thread for each client.
     * When clients successfully joins, thread terminates. If the game has already started, client is notified and ignored.
     * @param client - the client's socket
     */
    private void acceptClient(Socket client) {
        Thread acceptClient = new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                if (!controller.hasGameStarted()) {
                    out.println("[INFO] Welcome to MyShelfie! Press '/quit' to quit, '/chat ' to chat with other players..");

                    while (true) {
                        int resultValue = playerJoin(client);
                        if (resultValue == 0 || resultValue == -1) {    //successfully joined
                            server.addPlayerToConnectedClients(getNickFromSocket(client));
                            return;
                        }
                        if (resultValue == -4) {
                            //here if the player disconnected before the game is created
                            return;
                        }
                    }
                }else
                    out.println("[GAMEEND] Game has already started! Try to join again later.");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        acceptClient.start();
    }

    /**
     * Helper function for acceptClient. Lets client pick a nickname and - if first to join - create a new game
     * @return result of controller.joinGame()
     */
    private int playerJoin(Socket client) throws IOException, InterruptedException {
        String nickname;
        InputStream input = client.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        //asks player nickname
        boolean imbecille = false;
        boolean nicknameAlreadyInUse = false;
        out.println("[REQUEST] Choose a nickname:");
        do {
            try {
                if (imbecille) {
                    if (nicknameAlreadyInUse)
                        out.println("[REQUEST] Nickame already in use. Try again:");
                    else
                        out.println("[REQUEST] Invalid nickname. Try again:");
                }
                nickname = reader.readLine();
                if (nickname.equals("[PING]")) break;
                nicknameAlreadyInUse = false;

                for (int i = 0; i < server.clientsInLobby.size(); i++) {
                    if (Objects.equals(server.clientsInLobby.get(i), nickname)) {
                        nicknameAlreadyInUse = true;
                    }
                }

                if (nickname.length() > 15 || nickname.equals("") || nickname.contains("@") || nickname.startsWith("/") || nicknameAlreadyInUse)
                    imbecille = true;
                else
                    break;
            } catch (Exception e){    //TODO what does this try/catch do?
                imbecille = false;
            }
        } while (true);
        server.clientsInLobby.add(nickname);
        clients.add(new socketNickStruct(client, nickname));
        clientListener(client, nickname);
        sendMessage("[CONNECTED]", client);

        out.println("[INFO] Chosen nickname: " + nickname);
        if (controller.isGameBeingCreated) {
            out.println("[INFO] Game is being created by another player...");
            synchronized (this){
                while(!controller.hasGameBeenCreated() && !nickname.equals(server.clientsInLobby.get(0))) {
                    wait();
                    boolean disconnected = true;
                    for (int i = 0; i < clients.size(); i++) {
                        if(clients.get(i).getName().equals(nickname)){
                            disconnected = false;
                        }
                    }
                    if(disconnected)
                        return -4;
                }
            }
        }

        return joinGameSwitch(client, nickname, out, reader);
    }

    /**
     * Handles possible joining outcomes after calling controller.joinGame()
     */
    private synchronized int joinGameSwitch(Socket client, String nickname, PrintWriter out, BufferedReader reader) throws IOException, InterruptedException {
        boolean imbecille;
        switch (controller.joinGame(nickname)) {
            //no existing game
            case -1 -> {
                //gui deve andare in matchtype
                String line;
                imbecille = false;
                do {
                    if (imbecille)
                        out.println("[REQUEST] Invalid input, you can choose between 2 and 4 players: ");
                    else
                        out.println("[REQUEST] Choose the number of players for the game: ");
                    while(messageBuffer.isEmpty())
                        synchronized (this) {
                            wait();
                            if(!nickname.equals(server.clientsInLobby.get(0))){ //if this thread is notified and nickname of player isn't
                                return -4;                                      //equal to the first one in the list means this player
                            }                                                   //disconnected while choosing the number of players
                        }
                    line = messageBuffer.remove(0);

                    imbecille = true;
                    if(controller.hasGameBeenCreated()){
                        out.println("[INFO] Somebody has already created a Game!");
                        return joinGameSwitch(client, nickname, out, reader);
                    }

                }while (!isNumeric(line) || Integer.parseInt(line) < 2 || Integer.parseInt(line) > 4);

                if(controller.createNewGame(nickname, Integer.parseInt(line))){ //create new game
                    out.println("[INFO] Selected number of players for the game: " + line);
                    //clients.add(new socketNickStruct(client, nickname));
                    server.addPlayerToRecord(nickname, Server.connectionType.Socket);
                    out.println("[INFO] Waiting for all players to connect...");
                    return -1;
                }
                else{
                    out.println("[INFO] Somebody has already created a Game!");
                    //clients.add(new socketNickStruct(client, nickname));
                    server.addPlayerToRecord(nickname, Server.connectionType.Socket);
                    out.println("[INFO] Waiting for all players to connect...");
                    return 0;
                }

            }
            //game has started
            case -2 -> {
                //return to connectiontype
                return -2;
            }
            //name in use
            case -3 -> {        //should never reach thanks to the list in server "clientsInLobby"
                //gui remains in loginScene
                out.println("[INFO] Nickname in use, try another one:");
                return -3;
            }
            //successful
            case 0 -> {
                //goes to gamescene
                //clients.add(new socketNickStruct(client, nickname));
                server.addPlayerToRecord(nickname, Server.connectionType.Socket);
                return 0;
            }
        }
        return -4;  //should never reach!
    }

    /**
     * Creates a thread to listen and process clients' messages. All received strings are either processed or appended to a List<String> messageBuffer. In
     * the latter case notify() is called to wake up drawInquiry waiting on input from client.
     */
    public void clientListener(Socket client, String nickname){
        Thread clientListener = new Thread(() -> {
            try {
                String line;
                InputStream input = client.getInputStream();
                while (true) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    while ((line = reader.readLine()) != null) {
                        // if readLine() returns null, the client has disconnected
                        if (Objects.isNull(line)) {
                            controller.endGame();
                            notifyGameEnd(nickname);
                        }
                        synchronized (this) {
                            // processes PING message
                            if (line.equals("[PING]")) {
                                for (socketNickStruct s : clients)
                                    if (s.getName().equals(nickname)) {
                                        s.setLastPing(System.currentTimeMillis());
                                    }
                                // processes user's input
                            }
                            else
                                if (controller.hasGameStarted()) {
                                    if (controller.isMyTurn(nickname) && !line.startsWith("/chat ")) {
                                        messageBuffer.add(line);
                                        notify();
                                    }
                                }
                                // processes chat message
                                else if (line.startsWith("/chat ")) {
                                    String text = "", receiver = "";

                                    int atIndex;

                                    if (line.startsWith("/chat @")) {
                                        atIndex = line.indexOf('@');
                                        receiver = line.substring(atIndex + 1);
                                        atIndex = receiver.indexOf(' ');
                                        text = receiver.substring(atIndex + 1);
                                        receiver = receiver.substring(0, atIndex);
                                        if (!Objects.equals(receiver, nickname))
                                            sendChatMessageToClient(nickname, text, receiver, true);
                                    }
                                    else {
                                        receiver = "all";
                                        atIndex = line.indexOf(' ');
                                        text = line.substring(atIndex + 1);
                                        sendChatMessageToClient(nickname, text, receiver, false);
                                    }
                                }
                                // processes /quit
                                else if (line.equals("/quit")) {
                                    PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                                    pw.println("[REQUEST]: Are you sure you want to quit? (y/n): ");
                                    line = reader.readLine();
                                    if (line.equals("y")) {
                                        if (!controller.hasGameStarted()) {
                                            controller.removePlayer(nickname);
                                            pw.println("[GAMEEND]: You quit.");
                                        } else {
                                            controller.endGame();
                                            notifyGameEnd(nickname);
                                        }
                                        break;  //closes listener on confirmed quit
                                    }
                                }
                                else if (!controller.hasGameStarted()) {
                                    messageBuffer.add(line);
                                    notifyAll();
                                }
                        }
                    }
                }
            } catch (SocketException e){
                System.out.println(nickname + "'s socket has been closed.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientListener.start();
    }

    /**
     * This thread waits until the game starts, then periodically checks that all clients have sent a [PING] message
     * within DISCONNECTION_TIME seconds. If they haven't, it is assumed they disconnected and the game ends.
     * @throws IOException
     */
    public void checkForDisconnections() throws IOException {
        new Thread(() -> {
            try {
                while (Objects.isNull(controller)) Thread.sleep(500);
                while (true) {
                    if(controller.hasGameStarted()) {
                        //System.out.println(System.currentTimeMillis());
                        for (socketNickStruct client : clients) {
                            //System.out.println(client.getName() + ": " + client.getLastPing());
                            if (System.currentTimeMillis() - client.getLastPing() > DISCONNECTION_TIME) {
                                notifyGameEnd(client.getName());
                                controller.endGame();
                            }
                        }
                    }
                    else{
                        for (int i = 0; i<clients.size(); i++) {
                            socketNickStruct client = clients.get(i);

                            if (System.currentTimeMillis() - client.getLastPing() > DISCONNECTION_TIME) {
                                String nickOfDisconnectedPlayer = client.getName();

                                server.clientsInLobby.remove(nickOfDisconnectedPlayer);
                                //System.out.println(nickOfDisconnectedPlayer +" disconnected");
                                clients.remove(client);
                                server.notifyLobbyDisconnection();
                                //TODO chek more often for disconnection
                                i--;

                            }
                        }
                    }
                    //System.out.println("has the game ended: " + controller.hasTheGameEnded());
                    Thread.sleep(1000);
                }
                }catch(Exception e){
                    e.printStackTrace();
                }
        }).start();
    }

    /**
     * Queries the client for info on his turn's drawn tiles
     * @param nickname - the nickname of the client to query
     * @param b - game's board
     * @param shelf - client's board
     * @return drawInfo, a struct containing which tiles are drawn and the column where they are to be placed in client's shelf
     */
    public drawInfo drawInquiry(String nickname, Board b, Shelf shelf, PersonalGoalCard pgc, List<CommonGoalCard> cgc, List<Player> leaderboard) throws InvalidMoveException {
        Socket playerSocket = null;
        List<Tile> drawnTiles = new ArrayList<>();
        drawInfo drawInfo = new drawInfo();
        messageBuffer.clear();

        //find client's socket
        for (socketNickStruct c : clients)
            if (c.getName().equals(nickname)) {
                playerSocket = c.getSocket();
            }

        try {
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
            sendSerializedObjects(out, nickname, b, shelf, pgc, cgc, leaderboard);

            boolean imbecille = false;
            boolean invalidMoveFlag = false;
            String line;

            //do-while block handles correctly drawing tiles from board
            do {
                //asks for row
                do {
                    if (imbecille)
                        out.println("[REQUEST] Invalid Input! Select the row from which to draw from:");
                    else if (invalidMoveFlag)
                        out.println("[REQUEST] You cannot draw those tiles. Try again, insert row: ");
                    else
                        out.println("[YOUR TURN] Select the row from which to draw from:");
                    //waits for input
                    while(messageBuffer.isEmpty())
                        synchronized (this) {
                            wait();
                        }

                    line = messageBuffer.remove(0);
                    imbecille = true;
                } while (!isNumeric(line) || Integer.parseInt(line) > 8 || Integer.parseInt(line) < 0);
                drawInfo.setX(Integer.parseInt(line));
                imbecille = false;

                //asks for column
                do {
                    if (imbecille)
                        out.println("[REQUEST] Invalid Input! Select the column from which to draw from:");
                    else
                        out.println("[REQUEST] Select the column from which to draw from:");
                    //waits for input
                    while(messageBuffer.isEmpty())
                        synchronized (this) {
                            wait();
                        }
                    line = messageBuffer.remove(0);
                    imbecille = true;
                } while (!isNumeric(line) || Integer.parseInt(line) > 8 || Integer.parseInt(line) < 0);
                drawInfo.setY(Integer.parseInt(line));
                imbecille = false;

                //asks for tile quantity to be drawn
                do {
                    if (imbecille)
                        out.println("[REQUEST] Invalid Input! How many tiles do you want to draw?");
                    else
                        out.println("[REQUEST] How many tiles do you want to draw?");
                    //waits for input
                    while(messageBuffer.isEmpty())
                        synchronized (this) {
                            wait();
                        }
                    line = messageBuffer.remove(0);
                    imbecille = true;
                } while (!isNumeric(line) || Integer.parseInt(line) > 3 || Integer.parseInt(line) < 1);
                drawInfo.setAmount(Integer.parseInt(line));
                imbecille = false;

                //asks for tile direction
                if (drawInfo.getAmount() > 1) {
                    do {
                        if (imbecille)
                            out.println("[REQUEST] Invalid Input! In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                        else
                            out.println("[REQUEST] In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                        //waits for input
                        while(messageBuffer.isEmpty())
                            synchronized (this) {
                                wait();
                            }
                        line = messageBuffer.remove(0);
                        imbecille = true;
                    } while (!isNumeric(line) || Integer.parseInt(line) > 3 || Integer.parseInt(line) < 0);
                    drawInfo.setDirection(Board.Direction.values()[Integer.parseInt(line)]);
                    imbecille = false;
                }
                else {
                    drawInfo.setDirection(Board.Direction.RIGHT);
                }

                //try to draw selected tiles
                try {
                    drawnTiles = b.getTilesForView(drawInfo.getX(), drawInfo.getY(), drawInfo.getAmount(), drawInfo.getDirection());
                    invalidMoveFlag = false;
                } catch (InvalidMoveException e) {
                    invalidMoveFlag = true;
                }
            }while (invalidMoveFlag);

            //show drawn tiles
            String stringa = "[INFO]: Here are your tiles: ";
            int i = 1;
            for (Tile t : drawnTiles) {
                stringa += i + ")" + t + " ";
                i++;
            }
            out.println(stringa);

            //show shelf
            out.println("[SHELF] Here is your Shelf: ");
            String jsonShelf = gson.toJson(shelf);
            out.println("[GSONSHELF]" + jsonShelf);

            //asks shelf column to insert tiles
            boolean tooManyTiles = false;
            do {
                if (imbecille) {
                    if (tooManyTiles){
                        out.println("[REQUEST]: The tiles won't fit there! Try again: ");
                        tooManyTiles = false;
                    }
                    else out.println("[REQUEST] Invalid Input! Choose in which column you want to insert the tiles: [0 ... 4]");
                }
                else
                    out.println("[REQUEST] Choose in which column you want to insert the tiles: [0 ... 4]");
                while(messageBuffer.isEmpty())
                    synchronized (this) {
                        wait();
                    }
                line = messageBuffer.remove(0);
                if (!shelf.canItFit(drawInfo.getAmount(), Integer.parseInt(line))) tooManyTiles = true;
                imbecille = true;
            } while (!isNumeric(line) || Integer.parseInt(line) > 4 || Integer.parseInt(line) < 0 || tooManyTiles);
            drawInfo.setColumn(Integer.parseInt(line));

            //ask in which order to insert the tiles
            List<Tile> reorderedTiles  = new ArrayList<>();
            List<Integer> insertedValues = new ArrayList<>();
            if (drawInfo.getAmount() > 1)
                for (i = 0; i < drawInfo.getAmount(); i++) {
                    imbecille = false;
                    if (i == 0) {
                        do {
                            if (imbecille)
                                out.println("[REQUEST]: Invalid input! Try again with a valid value: ");
                            else
                                out.println("[REQUEST]: Choose which tile to insert first: [e.g. 1)C 2)G 3)T -> type 1 to insert C]");
                            while(messageBuffer.isEmpty())
                                synchronized (this) {
                                    wait();
                                }
                            line = messageBuffer.remove(0);
                            if (line.startsWith("[GUI]")) break;
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > drawInfo.getAmount() || Integer.parseInt(line) < 1);
                        if (line.startsWith("[GUI]")) break;
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line) - 1));
                        insertedValues.add(Integer.parseInt(line));
                    }
                    if (i == 1) {
                        do {
                            if (imbecille)
                                out.println("[REQUEST]: Invalid input! Try again with a valid value: ");
                            else
                                out.println("[REQUEST]: Choose which tile to insert next");
                            while(messageBuffer.isEmpty())
                                synchronized (this) {
                                    wait();
                                }
                            line = messageBuffer.remove(0);
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > drawInfo.getAmount() || Integer.parseInt(line) < 1 || insertedValues.contains(Integer.parseInt(line)));
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line) - 1));
                        insertedValues.add(Integer.parseInt(line));
                    }
                    if (i == 2) {
                        do {
                            if (imbecille)
                                out.println("[REQUEST]: Invalid input! Try again with a valid value: ");
                            else
                                out.println("[REQUEST]: Choose which tile to insert next");
                            while(messageBuffer.isEmpty())
                                synchronized (this) {
                                    wait();
                                }
                            line = messageBuffer.remove(0);
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > drawInfo.getAmount() || Integer.parseInt(line) < 1 || insertedValues.contains(Integer.parseInt(line)));
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line)-1));
                    }
                }
            else reorderedTiles = drawnTiles;
            if (line.startsWith("[GUI]")){
                // deserialize List<Position> sent by GUI, create corresponding List<Tile>
                line = line.replace("[GUI]", "");
                TypeToken<List<Position>> typeToken = new TypeToken<>() {};
                List<Position> positionList = gson.fromJson(line, typeToken.getType());
                List<Tile> tileList = new ArrayList<>();

                for (Position p: positionList){
                    tileList.add(controller.getTilePlacingSpot()[p.getX()][p.getY()].drawTileFromSpot());
                }
                reorderedTiles = tileList;
            }
            drawInfo.setTiles(reorderedTiles);
        } catch(IOException | InterruptedException e){
                e.printStackTrace();
        }
        return drawInfo;
    }

    /**
     * Sends serialized objects to specified PrintWriter
     * @param out the specifiec PrintWriter
     */
    private void sendSerializedObjects(PrintWriter out, String nickname, Board b, Shelf shelf, PersonalGoalCard pgc, List<CommonGoalCard> cgc, List<Player> leaderboard){
        //*************** SERIALIZATION ***************
        out.println("[NICKNAME]" + nickname);

        String jsonBoard = gson.toJson(b);
        out.println("[GSONBOARD]" + jsonBoard);

        String jsonShelf = gson.toJson(shelf);
        out.println("[GSONSHELF]" + jsonShelf);

        String jsonPersonalGoal = gson.toJson(pgc);
        out.println("[GSONPGC]" + jsonPersonalGoal);

        String jsonCommonGoal = gson.toJson(cgc);
        out.println("[GSONCGC]" + jsonCommonGoal);

        String jsonLeaderboard = gson.toJson(leaderboard);
        out.println("[GSONLEAD]" + jsonLeaderboard);

        String jsonPGMap = gson.toJson(controller.getPGCmap());
        out.println("[GSONPGMAP]" + jsonPGMap);
        //*********************************************
    }

    /**
     * @return the name of the corresponding client, null if not present
     */
    private String getNickFromSocket(Socket client){
        for (socketNickStruct c: clients) if (c.getSocket().equals(client)) return c.getName();
        return null;
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    /**
     * Prints message to all clients in socketNickStruct clients
     * @param message
     */
    public synchronized void broadcastMessage(String message){
        try {
            for (socketNickStruct c : clients) {
                PrintWriter pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                pw.println(message);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Prints message to the client that matches the 'Socket client' param in the 'socketNickStruct clients' list
     */
    private void sendMessage(String message, Socket client){
        try {
            for (socketNickStruct c : clients) {
                if (c.getSocket().equals(client)) {
                    PrintWriter pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                    pw.println(message);
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /**
     * Prints message to the client that matches the 'String nickname' param in the 'socketNickStruct clients' list
     */
    private void sendMessage(String message, String nickname){
        try {
            for (socketNickStruct c : clients) {
                if (c.getName().equals(nickname)) {
                    PrintWriter pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                    pw.println(message);
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to all clients, informing game has begun. Also sends the nickname of the current player.
     * @param nickname
     */
    public synchronized void notifyGameStart(String nickname) {
        try {
            for (socketNickStruct c : clients) {
                PrintWriter pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                sendSerializedObjects(pw, c.getName(), new Board(controller.getTilePlacingSpot()), new Shelf(controller.getMyShelf(c.getName())), controller.getPGC(c.getName()), controller.getCommonGoalCards(), controller.getLeaderboard());
                pw.println("[CURRENTPLAYER]" + nickname);
                pw.println("[INFO]: Game is starting. " + nickname + "'s turn.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void notifyGameEnd(String nick) throws IOException {
        for (socketNickStruct c: clients){
            PrintWriter out = new PrintWriter(c.getSocket().getOutputStream(), true);
            out.println("[GAMEEND]: " + nick + " has quit the game. The game has ended.");
            c.getSocket().close();
        }
    }

    /**
     * This method is used to update the client at the end of their turn - it informs them the turn has successfully ended and shows them their updated shelf
     * @param updatedShelf client's shelf after tiles drawn from board are inserted
     * @param nickname client's nick
     */
    public void turnEnd(Shelf updatedShelf, String nickname){
        Socket playerSocket = null;
        Gson gson = new Gson();

        //find client's socket
        for (socketNickStruct c: clients)
            if (c.getName().equals(nickname))
                playerSocket = c.getSocket();

        try {
            PrintWriter pw = new PrintWriter(playerSocket.getOutputStream(), true);
            String jsonShelf = gson.toJson(updatedShelf);
            pw.println("[GSONSHELF]" + jsonShelf);
            pw.println("[TURNEND] Your turn has ended! Waiting for next player.");
            updateGameObjectsAfterTurn();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Sends all clients a serialized copy of the game board and of the leaderboard. Also sends the nick of the current player - this notifies that the turn has ended.
     * Note: leaderboard is a List<Player>, so it contains
     * all players' instance fields.
     */
    private void updateGameObjectsAfterTurn(){
        try {
            for (socketNickStruct s : clients) {
                PrintWriter pw = new PrintWriter(s.getSocket().getOutputStream(), true);
                String jsonBoard = gson.toJson(controller.getBoard());
                pw.println("[GSONBOARD]" + jsonBoard);

                String jsonLeaderboard = gson.toJson(controller.getLeaderboard());
                pw.println("[GSONLEAD]" + jsonLeaderboard);

                pw.println("[CURRENTPLAYER]" + controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     *  Creates new instance of clients array
     */
    public void flushServer(){
            clients = new ArrayList<>();
    }

    public void setController(Controller c){ this.controller = c;}

    public void sendChatMessageToClient(String sender, String text, String receiver, Boolean pm) throws IOException {
        if(clients.stream().noneMatch(client->client.getName().equals(receiver)))
            server.chatMessage(sender, text, receiver, pm);
        else
            for (socketNickStruct c: clients){
                if(c.getName().equals(receiver)){
                    PrintWriter out = new PrintWriter(c.getSocket().getOutputStream(), true);
                    if(pm)
                        out.println("[MESSAGE FROM "+sender+" TO YOU]: "+text);
                    else
                        out.println("[MESSAGE FROM "+sender+"]: "+text);
                }
            }
    }
    public void notifyLobbyDisconnectionSocket(){
        //controller.isGameBeingCreated = false;
        synchronized (this){
            notifyAll();
        }

    }
    public void notifyGameHasBeenCreatedSocket(){
        synchronized (this){
            notifyAll();
        }
    }
}
//TODO server cant handle when first player to connect and choose num of player disconnects