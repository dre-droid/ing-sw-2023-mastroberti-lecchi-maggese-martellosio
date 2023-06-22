package Server.Socket;


import GUI.PositionStuff.Position;
import Server.Controller;
import Server.Server;
import Server.ClientInfoStruct;
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
import java.nio.Buffer;
import java.rmi.RemoteException;
import java.util.*;

public class ServerSock {

    private ArrayList<socketNickStruct> clients = new ArrayList<>();
    private Controller controller;
    private final Server server;
    private final long DISCONNECTION_TIME = 10000;  //disconnection threshold: 10s
    private final Gson gson = new GsonBuilder().registerTypeAdapter(StrategyCommonGoal.class, new StrategyAdapter()).create();
    public List<String> messageBuffer = new ArrayList<>();
    public final Object clientsLock = new Object();

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
                    //checkForDisconnections();
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
     * @param client - the client's Socket
     */
    private void acceptClient(Socket client) {
        Thread acceptClient = new Thread(() -> {
            try {
                playerJoin(client);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        acceptClient.start();
    }

    /**
     * Helper function for acceptClient. Lets client pick a nickname and, if the nickname isn't in use by any other player,
     * adds him to the ArrayLists server.clientsLobby and clients and starts the method clientListener() to listen to
     * the inputs sent by that client and checkForDisconnectionV2 to check if the client is still connected to the server.
     * If the nickname is already in use and there's an ongoing match but the client who was holding that nickname has
     * disconnected, then the method rejoin() will be called to allow a disconnected player to rejoin a game by simply
     * using the same nickname
     * @author Diego Lecchi, Andrea Mastroberti
     * @param client - the client's Socket
     */
    private void playerJoin(Socket client) throws IOException, InterruptedException {
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
                        out.println("[REQUEST] Nickname already in use. Try again:");
                    else
                        out.println("[REQUEST] Invalid nickname. Try again:");
                }
                nickname = reader.readLine();
                if (nickname.equals("[PING]")) break;
                nicknameAlreadyInUse = false;

                for (int i = 0; i < server.clientsLobby.size(); i++) {  //looks if the name is in use by other players
                    if (server.clientsLobby.get(i).getNickname().equals(nickname)) {
                        nicknameAlreadyInUse = true;
                        if(server.clientsLobby.get(i).isDisconnected()) {   //if nickname is present in clientsLobby but is disconnected
                            rejoinGame(nickname, client, reader, out);      //calls reJoinGame
                            return;
                        }
                    }
                }

                if (nickname.length() > 15 || nickname.equals("") || nickname.contains("@") || nickname.contains(" ") ||
                        nickname.startsWith("/") || nickname.equals("Server") || nicknameAlreadyInUse)
                    imbecille = true;
                else
                    break;
            } catch (Exception e){    //TODO what does this try/catch do?
                imbecille = false;
            }
        } while (true);
        if(controller.hasGameStarted()){
            out.println("[INFO] The game already started, you can't join, try again later");
            //out.println("[EXIT]");
            return;
        }


        server.clientsLobby.add(new ClientInfoStruct(nickname));
        for (int i = 0; i < server.clientsLobby.size(); i++) {
            if (nickname.equals(server.clientsLobby.get(i).getNickname())) {
                server.clientsLobby.get(i).setSocket(client);
                server.clientsLobby.get(i).setOut(out);
                server.clientsLobby.get(i).setReader(reader);
            }
        }
        server.notifyServer();
        synchronized (clientsLock){
            clients.add(new socketNickStruct(client, nickname));
            for (int i = 0; i < clients.size(); i++) {
                if(clients.get(i).getName().equals(nickname))
                    checkForDisconnectionsV2(clients.get(i));
            }
        }

        clientListener(client, nickname, reader, out);
        sendMessage("[CONNECTED]", client);
        sendMessage("[NICKNAME]" + nickname, client);
        if (controller.isGameBeingCreated && !server.clientsLobby.get(0).getNickname().equals(nickname))
            out.println("[INFO] Game is being created by another player...");

    }

    /**
     * This method is called by Server, it's used to join a game by calling the method controller.joinGame(nickname).
     * Based on the value returned by that function it will either join a game (0), create a new game by choosing the
     * number of player and joining that game (-1) or warns that the game has already started, and therefore it will
     * remove the player from the ArrayLists clients and from server.clientsLobby by calling server.removeFromClientsLobby(nickname)
     * @param nickname - the nickname of the client
     * @param out - the PrintWriter of the client
     * @throws InterruptedException
     * @throws IOException
     * @author Diego Lecchi, Andrea Mastroberti
     */
    public void joinGame(String nickname, PrintWriter out) throws InterruptedException, IOException{
        new Thread(() -> {
            boolean imbecille;
            try {
                switch (controller.joinGame(nickname)) {
                    case -1 -> {    //there is not any game to join
                        String line;
                        imbecille = false;
                        do {
                            if (imbecille)
                                out.println("[REQUEST] Invalid input, you can choose between 2 and 4 players: ");
                            else
                                out.println("[REQUEST] Choose the number of players for the game: ");
                            while (messageBuffer.isEmpty())
                                synchronized (this) {
                                    wait();
                                    if (!nickname.equals(server.clientsLobby.get(0).getNickname())) {
                                        //if this thread is notified and nickname of player isn't equal to the first one in the
                                        // list means this player disconnected while choosing the number of players
                                        return;
                                    }
                                }
                            line = messageBuffer.remove(0);
                            imbecille = true;

                        } while (!isNumeric(line) || Integer.parseInt(line) < 2 || Integer.parseInt(line) > 4);
                        controller.createNewGame(nickname, Integer.parseInt(line));
                        out.println("[INFO] Selected number of players for the game: " + line);
                        server.addPlayerToRecord(nickname, Server.connectionType.Socket);
                        out.println("[INFO] Waiting for all players to connect...");
                    }
                    case -2 -> {    //the game has already started
                        out.println("[INFO] The game already started, you can't join, try again later");
                        out.println("[EXIT]");
                        clients.removeIf(socketNickStruct -> nickname.equals(socketNickStruct.getName()));
                        //server.clientsMap.remove(nickname);
                        server.removeFromClientsLobby(nickname);
                    }
                    case 0 -> {     //the player joined the game correctly
                        server.addPlayerToRecord(nickname, Server.connectionType.Socket);
                        out.println("[INFO] Joined a Game");
                        server.notifyServer();
                        server.broadcastMessage("Player " + nickname + " joined the game!", nickname);
                    }
                }
            } catch (RemoteException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Called by playerJoin() when a player chooses the same nickname as a disconnected player, it updates the Socket
     * and sets the Boolean disconnected to false in the respective ClientInfoStruct object in Server.clientsLobby, adds
     * client to server.clientsMap launches clientListener(), then updates the respective socketNickStruct object in the
     * Arraylist clients with the Socket client passed as parameter, sets lastPing to the current time and then launches
     * checkForDisconnectionV2 and notifies Server
     * @author Diego Lecchi
     * @param nickname - nickname of the client rejoining a game
     * @param client - Socket of the client
     * @param reader - BufferedReader of the client
     */
    public void rejoinGame(String nickname, Socket client, BufferedReader reader, PrintWriter out){
        for (int i = 0; i < server.clientsLobby.size(); i++) {
            if (server.clientsLobby.get(i).getNickname().equals(nickname)){         //search for the object in server.clientsLobby with the same nickname
                server.clientsLobby.get(i).setSocket(client);                       //updates the Socket
                server.clientsLobby.get(i).setDisconnected(false);                  //set boolean disconnected to false
                server.addPlayerToRecord(nickname, Server.connectionType.Socket);   //adds client to server.clientsMap
                clientListener(client, nickname, reader, out);                           //launches clientListener
                break;
            }
        }
        if(clients.stream().anyMatch(socketNickStruct -> socketNickStruct.getName().equals(nickname))) {    //if the same nickname already exists in clients
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getName().equals(nickname)) {
                    clients.get(i).setSocket(client);                           //update Socket
                    clients.get(i).setLastPing(System.currentTimeMillis());     //set lastPing to current time
                    checkForDisconnectionsV2(clients.get(i));                   //launch check for disconnection
                    sendMessage("[CONNECTED]", client);
                    sendMessage("[INFO] You have successfully rejoined the game, wait for your turn", client);
                    server.notifyServer();                                      //notify server
                    server.broadcastMessage("Player " + nickname + " rejoined the game!", nickname);
                    notifyGameStart(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
                    break;
                }
            }
        }
        else{                           //otherwise the client is trying to rejoin a saved game, so we need to add a new socketNickStruct in clients
            synchronized (clientsLock){
                clients.add(new socketNickStruct(client, nickname));
                for (int i = 0; i < clients.size(); i++) {
                    if(clients.get(i).getName().equals(nickname)) {
                        clients.get(i).setLastPing(System.currentTimeMillis()); //set lastPing to current time
                        checkForDisconnectionsV2(clients.get(i));               //launch check for disconnection
                        break;
                    }
                }
            }
            sendMessage("[CONNECTED]", client);
            sendMessage("[INFO] You have successfully rejoined the game, wait for all players to rejoin", client);
            try {
                if(server.loadedFromFile)
                    server.serverRMI.tryToStartLoadedGame();
            } catch (RemoteException e) {
                System.out.println("cannot start loaded game");
            }
            server.notifyServer();  //notify server
            server.broadcastMessage("Player " + nickname + " rejoined the game!", nickname);
        }
    }

    /**
     * Creates a thread to listen and process a client messages. All received strings are either processed or appended
     * to a List<String> messageBuffer. In the latter case notify() is called to wake up drawInquiry waiting on input from client.
     * @author Diego Lecchi, Andrea Mastroberti
     * @param client - Socket of the client
     * @param nickname - nickname of the client
     * @param reader - BufferedReader of the client
     */
    public void clientListener(Socket client, String nickname, BufferedReader reader, PrintWriter out){
        Thread clientListener = new Thread(() -> {
            try {
                PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                String line;
                while (true) {
                    while ((line = reader.readLine()) != null) {

                        synchronized (this) {
                            // processes PING message
                            if (line.equals("[PING]")) {
                                for (socketNickStruct s : clients) {
                                    if (s.getName().equals(nickname)) {
                                        s.setLastPing(System.currentTimeMillis());
                                    }
                                    out.println("[PING]");
                                }
                                // processes user's input
                            }
                            else if (controller.hasGameStarted()) { //if the game started

                                if (controller.isMyTurn(nickname) && !line.startsWith("/chat ") && !line.equals("/quit")) {
                                    messageBuffer.add(line);
                                    notifyAll();
                                }
                                // processes chat message
                                else if (line.startsWith("/chat ")) {
                                    chatHandler(nickname, line);
                                }

                                    // processes /quit
                                else if (line.equals("/quit")) {    //TODO needs to advise everyone who quitted the game "player1 quitted"
                                    controller.endGame();
                                    break;  //closes listener on confirmed quit
                                }
                            }

                            else if (!controller.hasGameStarted()) {    //if the game hasn't started yet
                                if(!line.startsWith("/chat ") && !line.equals("/quit")) {
                                    messageBuffer.add(line);
                                    notifyAll();
                                }
                                else if (line.startsWith("/chat ")) {
                                    chatHandler(nickname, line);
                                }
                                else if (line.equals("/quit")) {
                                    if(controller.hasGameBeenCreated())
                                        controller.removePlayer(nickname);
                                    server.notifyLobbyDisconnection(nickname);
                                    server.clientsMap.remove(nickname);
                                    clients.removeIf(c -> c.getName().equals(nickname));
                                    pw.println("[GAMEEND]: You quit.");
                                    break;
                                }
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
     * Handles chat messages sent by a client. If the message starts with "/chat @" then the character after "@" and before
     * " " wil be treated as the nickname of the receiver when the method sendMessageToClient() is called, otherwise
     * receiver = "all" and the message will be sent to all players connected
     * @author Diego Lecchi
     * @param nickname - nickname of the client
     * @param line - message of the client
     * @throws IOException
     */
    private void chatHandler(String nickname, String line) throws IOException {
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

    /**
     * Periodically checks if the System.currentTimeMillis() minus the attribute lastPing of the respective socketNickStruct
     * client in the ArrayList clients is greater than a certain amount of milliseconds, if so the client will be declared
     * disconnected and:
     * - if the game hasn't been created yet, the client will be removed from Server.clientsLobby and clients
     * - if the game has been created but note yet started the player will be removed from the Game playersList,
     *   server.clientsMap, clients and server.clientsLobby through server.notifyLobbyDisconnection
     * - if the game started Boolean disconnected in the respective ClientInfoStruct in Server.clientsLobby will be set
     *   to true and server notified of a disconnection
     * @author Diego Lecchi
     * @param client - Socket of the client
     */
    public void checkForDisconnectionsV2(socketNickStruct client) {
        new Thread(() -> {
            try{
                while (true) {
                    //if game hasn't been created yet
                    if (!controller.hasGameBeenCreated()) {
                        if (System.currentTimeMillis() - client.getLastPing() > 3000){
                            server.notifyLobbyDisconnection(client.getName());          //notifies server that a disconnection has occurred
                            clients.remove(client);                                     // removes client from clients ArrayList here in serverSock
                            return;
                        }
                    }
                    //else if player is in the game
                    else {
                        if(!clients.contains(client)){
                            return;
                        }
                        //if game hasn't started yet
                        if (controller.getGamePlayerListNickname().contains(client.getName()) && !controller.hasGameStarted()) {
                            if (System.currentTimeMillis() - client.getLastPing() > 5000) {
                                controller.removePlayer(client.getName());
                                server.notifyLobbyDisconnection(client.getName());
                                server.clientsMap.remove(client.getName());
                                clients.remove(client);
                                return; //todo server deve mandare un messaggio che si e' disconnesso qualcuno tipo messaggio chat
                            }
                        }
                        //if player is in the game and the game started
                        else {
                            if (System.currentTimeMillis() - client.getLastPing() > 5000){
                                for (int i = 0; i < server.clientsLobby.size(); i++) {
                                    if(server.clientsLobby.get(i).getNickname().equals(client.getName())){
                                        server.clientsLobby.get(i).setDisconnected(true);
                                        server.notifyLobbyDisconnection(client.getName());
                                        return;
                                    }
                                }

                            }
                        }
                    }
                    Thread.sleep(500);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
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
        //finds ClientInfoStruct in server.clientsLobby
        ClientInfoStruct clientInfoStruct = null;
        for (int i = 0; i < server.clientsLobby.size(); i++) {
            if(server.clientsLobby.get(i).getNickname().equals(nickname))
                clientInfoStruct = server.clientsLobby.get(i);
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
                    while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                        synchronized (this) {
                            wait();
                        }
                    if (clientInfoStruct.isDisconnected())
                        return null;

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
                    while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                        synchronized (this) {
                            wait();
                        }
                    if (clientInfoStruct.isDisconnected())
                        return null;

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
                    while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                        synchronized (this) {
                            wait();
                        }
                    if (clientInfoStruct.isDisconnected())
                        return null;

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
                        while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                            synchronized (this) {
                                wait();
                            }
                        if (clientInfoStruct.isDisconnected())
                            return null;

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
                    else out.println("[REQUEST] Invalid Input! Choose in which column you want to insert the tiles: [1 ... 5]");
                }
                else
                    out.println("[REQUEST] Choose in which column you want to insert the tiles: [1 ... 5]");
                while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                    synchronized (this) {
                        wait();
                    }
                if (clientInfoStruct.isDisconnected())
                    return null;

                line = messageBuffer.remove(0);
                if (!shelf.canItFit(drawInfo.getAmount(), Integer.parseInt(line) - 1)) tooManyTiles = true;
                imbecille = true;
            } while (!isNumeric(line) || Integer.parseInt(line) > 5 || Integer.parseInt(line) < 1 || tooManyTiles);
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
                            while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                                synchronized (this) {
                                    wait();
                                }
                            if (clientInfoStruct.isDisconnected())
                                return null;

                            line = messageBuffer.remove(0);
                            if (line.startsWith("[GUI]")) break;
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > drawInfo.getAmount() || Integer.parseInt(line) < 1);
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line) - 1));
                        insertedValues.add(Integer.parseInt(line));
                    }
                    if (i == 1) {
                        do {
                            if (imbecille)
                                out.println("[REQUEST]: Invalid input! Try again with a valid value: ");
                            else
                                out.println("[REQUEST]: Choose which tile to insert next");
                            while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                                synchronized (this) {
                                    wait();
                                }
                            if (clientInfoStruct.isDisconnected())
                                return null;

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
                            while(messageBuffer.isEmpty() && !clientInfoStruct.isDisconnected())
                                synchronized (this) {
                                    wait();
                                }
                            if (clientInfoStruct.isDisconnected())
                                return null;

                            line = messageBuffer.remove(0);
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > drawInfo.getAmount() || Integer.parseInt(line) < 1 || insertedValues.contains(Integer.parseInt(line)));
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line)-1));
                    }
                }
            else reorderedTiles = drawnTiles;
            drawInfo.setTiles(reorderedTiles);
            // send client drawn tiles
            out.println("[DRAWNTILES]" + gson.toJson(reorderedTiles));
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
        if (nickname != null)
            out.println("[NICKNAME]" + nickname);

        if (b!= null) {
            String jsonBoard = gson.toJson(b);
            out.println("[GSONBOARD]" + jsonBoard);
        }
        if (shelf != null) {
            String jsonShelf = gson.toJson(shelf);
            out.println("[GSONSHELF]" + jsonShelf);
        }
        if (pgc != null) {
            String jsonPersonalGoal = gson.toJson(pgc);
            out.println("[GSONPGC]" + jsonPersonalGoal);
        }
        if (cgc != null) {
            String jsonCommonGoal = gson.toJson(cgc);
            out.println("[GSONCGC]" + jsonCommonGoal);
        }
        if (leaderboard != null) {
            String jsonLeaderboard = gson.toJson(leaderboard);
            out.println("[GSONLEAD]" + jsonLeaderboard);
        }
        String jsonPGMap = gson.toJson(controller.getPGCmap());
        out.println("[GSONPGMAP]" + jsonPGMap);
        //*********************************************
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
    public void broadcastMessage(String message, String sender){
        try {
            for (socketNickStruct c : clients) {
                PrintWriter pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                if(!sender.equals(c.getName()))
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
    public void notifyGameStart(String nickname) {
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

    public void notifyGameEnd(/*String nick*/) throws IOException {
        for (socketNickStruct c: clients){
            PrintWriter out = new PrintWriter(c.getSocket().getOutputStream(), true);
            sendSerializedObjects(out, null, null, null, null, null, controller.getLeaderboard());
            out.println("[GAMEEND]: The game has ended.");
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
     * Sends all clients a serialized copy of the game board and of the leaderboard. Also sends the nick of the current
     * player - this notifies that the turn has ended.
     * Note: leaderboard is a List<Player>, so it contains all players' instance fields.
     */
    public void updateGameObjectsAfterTurn(){
        try {
            for (socketNickStruct s : clients) {
                PrintWriter pw = new PrintWriter(s.getSocket().getOutputStream(), true);
                String jsonBoard = gson.toJson(controller.getBoard());
                pw.println("[GSONBOARD]" + jsonBoard);
                System.out.println("Serialized board");

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