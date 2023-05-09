package Server.Socket;


import Server.Controller;
import Server.Server;
import com.google.gson.Gson;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ServerSock {

    public ArrayList<socketNickStruct> clients = new ArrayList<>();
    private Controller controller;
    private final Server server;
    public String string = "";  //used to communicate with playing player

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
                    while (true) {
                        Socket client = serverSocket.accept();
                        acceptClient(client);
                    }
                }catch (IOException e) {e.printStackTrace();}
            });
            runServer.start();
    }

    public void notifyGameEnd(String nick) throws IOException {
        //find client's socket
        for (socketNickStruct c: clients){
            PrintWriter out = new PrintWriter(c.getSocket().getOutputStream(), true);
            out.println("[GAMEEND]: " + nick + " has quit the game. The game has ended.");
            c.getSocket().close();
        }
    }

    /**
     * Creates thread to let a client join the game (thread allows multiple connections simultaneously). Adds client's socket to
     * List<socketNickStruct> clients if successful. Also creates a clientListener thread for each client.
     * When clients successfully joins, thread terminates.
     * @param client - the client's socket
     */
    private void acceptClient(Socket client) {
        Thread acceptClient = new Thread(() -> {
            try {
                boolean repeat = true;
                while (repeat) {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("[INFO]: Welcome to MyShelfie! Press '/quit' to quit, '/chat' to chat with other players..");

                    int resultValue = playerJoin(client);
                    if (resultValue == 0 || resultValue == -1) {    //successfully joined
                        repeat = false;
                        clientListener(client, getNickFromSocket(client));
                    }
                }
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
        out.println("[REQUEST] Inserisci un nickname:");
        do {
            if (imbecille) {
                out.println("[REQUEST] Nickname is too long! Try a shorter one:");
                notifyAll();
            }
            try {
                nickname = reader.readLine();
                if (nickname.length() > 15) imbecille = true;
                else break;
            }
            catch (Exception e){
                imbecille = false;
            }
        } while (true);
        out.println("Il nickname inserito è: "+ nickname);

        switch (controller.joinGame(nickname)) {
            //no existing game
            case -1 -> {
                String line;
                imbecille = false;
                do {
                    if (imbecille)
                        out.println("[REQUEST]: Invalid input, you can choose between 2 and 4 players: ");
                    else
                        out.println("[REQUEST]: Choose the number of players for the game: ");
                    line = reader.readLine();
                    imbecille = true;
                }while (!isNumeric(line) || Integer.parseInt(line) < 2 || Integer.parseInt(line) > 4);

                controller.createNewGame(nickname, Integer.parseInt(line)); //create new game
                out.println("[INFO]: Il numero di giocatori inserito è:  " + line);
                clients.add(new socketNickStruct(client, nickname));
                server.addPlayerToRecord(nickname, Server.connectionType.Socket);
                out.println("[INFO]: In attesa di altri giocatori.");

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
                server.addPlayerToRecord(nickname, Server.connectionType.Socket);
                return 0;
            }
        }
        return -4;  //should never reach!
    }

    /**
     * Creates a thread to listen to the clients' messages. If client's turn, writes client's messages to global variable string. If message starts with
     * '/c' processes message for chat, otherwise output is ignored.
     */
    public void clientListener(Socket client, String nickname){
        Thread clientListener = new Thread(() -> {
            try {
                String line;
                InputStream input = client.getInputStream();
                while (true) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    line = reader.readLine();

                    if (controller.hasGameStarted())
                        if (controller.isMyTurn(nickname) && !line.startsWith("/chat"))
                            string = line;

                    if (line.startsWith("/chat")){
                        String sender, text, receiver;
                        int atIndex = line.indexOf('@');
                        receiver = line.substring(atIndex+1);
                        atIndex = receiver.indexOf(' ');
                        text = receiver.substring(atIndex+1);
                        receiver = receiver.substring(0, atIndex);
                        sendChatMessageToClient(nickname, text, receiver);
                    }

                    if (line.equals("/quit")) {
                        PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
                        pw.println("[REQUEST]: Are you sure you want to quit? (y/n): ");
                        line = reader.readLine();
                        if (line.equals("y")) {
                            if (!controller.hasGameStarted()) {
                                controller.removePlayer(nickname);
                                pw.println("[GAMEEND]: You quit.");
                            } else {
                                controller.endGame();
                                for (socketNickStruct c : clients) {
                                    pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                                    pw.println("[GAMEEND]: " + nickname + " has quit the game. Game has ended.");
                                    c.getSocket().close();
                                }
                            }
                            break;  //closes listener on confirmed quit
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

        //find client's socket
        for (socketNickStruct c : clients)
            if (c.getName().equals(nickname)) {
                playerSocket = c.getSocket();
            }

        try {
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);

            //*************** SERIALIZATION ***************
            Gson gson = new Gson();

            out.println("[NICKNAME]" + nickname);

            String jsonBoard = gson.toJson(b);
            out.println("[GSONBOARD]" + jsonBoard);

            String jsonShelf = gson.toJson(shelf);
            out.println("[GSONSHELF]" + jsonShelf);

            String jsonPersonalGoal = gson.toJson(pgc.toString());
            out.println("[GSONPGC]" + jsonPersonalGoal);

            String jsonCommonGoal = cgc.get(0).getDescription() + "\n";
            jsonCommonGoal += cgc.get(1).getDescription() + "\n";
            jsonCommonGoal = gson.toJson(jsonCommonGoal);
            out.println("[GSONCGC]" + jsonCommonGoal);

            ArrayList<String> stringLeaderboard = new ArrayList<String>();
            for (Player p : leaderboard) stringLeaderboard.add(p.getNickname() + ": " + p.getScore());
            String jsonLeaderboard = gson.toJson(stringLeaderboard);
            out.println("[GSONLEAD]" + jsonLeaderboard);
            //*********************************************

            boolean imbecille = false;
            String line;
            boolean invalidMoveFlag = false;

            //do-while block handles correctly drawing tiles from board
            do {
                //asks for row
                do {
                    Thread.sleep(50);
                    if (!controller.hasTheGameEnded()) {
                        if (imbecille)
                            out.println("[REQUEST] Invalid Input! Select the row from which to draw from:");
                        else if (invalidMoveFlag)
                            out.println("[REQUEST] You cannot draw those tiles. Try again, insert row: ");
                        else
                            out.println("[YOUR TURN] Select the row from which to draw from:");
                    }
                    while (Objects.equals(string, "")) {
                        if (controller.hasTheGameEnded()) return null;
                        Thread.sleep(100);
                    }
                    line = string;
                    string = "";
                    if (line.equals("q") || controller.hasTheGameEnded()) return null;
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
                    while (Objects.equals(string, "")) {
                        if (controller.hasTheGameEnded()) return null;
                        Thread.sleep(100);
                    }
                    line = string;
                    string = "";
                    if (line.equals("q") || controller.hasTheGameEnded()) return null;
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
                    while (Objects.equals(string, "")) {
                        if (controller.hasTheGameEnded()) return null;
                        Thread.sleep(100);
                    }
                    line = string;
                    string = "";
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
                        while (Objects.equals(string, "")) {
                            if (controller.hasTheGameEnded()) return null;
                            Thread.sleep(100);
                        }
                        line = string;
                        string = "";
                        imbecille = true;
                    } while (!isNumeric(line) || Integer.parseInt(line) > 3 || Integer.parseInt(line) < 0);
                    drawInfo.setDirection(Board.Direction.values()[Integer.parseInt(line)]);
                    imbecille = false;
                    try {
                        drawnTiles = b.getTilesForView(drawInfo.getX(), drawInfo.getY(), drawInfo.getAmount(), drawInfo.getDirection());
                        invalidMoveFlag = false;
                    } catch (InvalidMoveException e) {
                        invalidMoveFlag = true;
                    }
                }
                else {
                    drawInfo.setDirection(Board.Direction.RIGHT);
                    try {
                        drawnTiles = b.getTilesForView(drawInfo.getX(), drawInfo.getY(), drawInfo.getAmount(), drawInfo.getDirection());
                        invalidMoveFlag = false;
                    } catch (InvalidMoveException e) {
                        invalidMoveFlag = true;
                    }
                }
            }while (invalidMoveFlag);


            String stringa = "[INFO]: Here are your tiles: ";
            int i = 1;
            for (Tile t : drawnTiles) {
                stringa += i + ")" + t + " ";
                i++;
            }
            out.println(stringa);

            out.println("[SHELF] Here is your Shelf: ");
            jsonShelf = gson.toJson(shelf);
            out.println("[GSONSHELF]" + jsonShelf);

            //asks shelf column to insert tiles
            boolean tooManyTiles = false;
            do {
                if (imbecille) {
                    if (tooManyTiles) out.println("[REQUEST]: The tiles won't fit there! Try again: ");
                    else out.println("[REQUEST] Invalid Input! Choose in which column you want to insert the tiles: [0 ... 4]");
                }
                else
                    out.println("[REQUEST] Choose in which column you want to insert the tiles: [0 ... 4]");
                while (Objects.equals(string, "")) {
                    if (controller.hasTheGameEnded()) return null;
                    Thread.sleep(100);
                }
                line = string;
                string = "";
                if (line.equals("q") || controller.hasTheGameEnded()) return null;
                if (!shelf.canItFit(drawInfo.getAmount(), Integer.parseInt(line))) tooManyTiles = true;
                imbecille = true;
            } while (!isNumeric(line) || Integer.parseInt(line) > 4 || Integer.parseInt(line) < 0 || tooManyTiles);
            drawInfo.setColumn(Integer.parseInt(line));
            imbecille = false;

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
                            while (Objects.equals(string, "")) {
                                if (controller.hasTheGameEnded()) return null;
                                Thread.sleep(100);
                            }
                            line = string;
                            string = "";
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > 3 || Integer.parseInt(line) < 1);
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line) - 1));
                        insertedValues.add(Integer.parseInt(line));
                    }
                    if (i == 1) {
                        do {
                            if (imbecille)
                                out.println("[REQUEST]: Invalid input! Try again with a valid value: ");
                            else
                                out.println("[REQUEST]: Choose which tile to insert next");
                            while (Objects.equals(string, "")) {
                                if (controller.hasTheGameEnded()) return null;
                                Thread.sleep(100);
                            }
                            line = string;
                            string = "";
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > 3 || Integer.parseInt(line) < 1 || insertedValues.contains(Integer.parseInt(line)));
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line) - 1));
                        insertedValues.add(Integer.parseInt(line));
                    }
                    if (i == 2) {
                        do {
                            if (imbecille)
                                out.println("[REQUEST]: Invalid input! Try again with a valid value: ");
                            else
                                out.println("[REQUEST]: Choose which tile to insert next");
                            while (Objects.equals(string, "")) {
                                if (controller.hasTheGameEnded()) return null;
                                Thread.sleep(100);
                            }
                            line = string;
                            string = "";
                            imbecille = true;
                        } while (!isNumeric(line) || Integer.parseInt(line) > 3 || Integer.parseInt(line) < 1 || insertedValues.contains(Integer.parseInt(line)));
                        reorderedTiles.add(drawnTiles.get(Integer.parseInt(line) - 1));
                    }
                }
            else reorderedTiles = drawnTiles;
            drawInfo.setTiles(reorderedTiles);
        } catch(IOException | InterruptedException e){
                e.printStackTrace();
        }
        return drawInfo;
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
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }


    public void notifyGameStart(String nickname) {
        try {
            for (socketNickStruct c : clients) {
                PrintWriter pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                pw.println("[INFO]: Game is starting. " + nickname + "'s turn.");
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

    public void sendChatMessageToClient(String sender, String text, String receiver) throws IOException {
        if(clients.stream().noneMatch(client->client.getName().equals(receiver)))
            server.chatMessage(sender, text, receiver);
        else
            for (socketNickStruct c: clients){
                if(c.getName().equals(receiver)){
                    /*System.out.println(sender);
                    System.out.println(text);
                    System.out.println(receiver);*/
                    PrintWriter out = new PrintWriter(c.getSocket().getOutputStream(), true);
                    out.println("[MESSAGE_FROM_"+sender+"]: "+text);
                }
            }
    }

    public boolean hasDisconnectionOccurred(){
        for (socketNickStruct c: clients){
            if (c.getSocket().isClosed()){
                controller.endGame();
                return true;
            }
        }
        return false;
    }

    /**
     * @return the name of the player that disconnected if present
     */
    public String getNameOfDisconnection(){
        for (socketNickStruct c: clients)
            if (c.getSocket().isClosed()) return c.getName();
        return null;
    }

    /**
     * @return the name of the corresponding client, null if not present
     */
    private String getNickFromSocket(Socket client){
        for (socketNickStruct c: clients) if (c.getSocket().equals(client)) return c.getName();
        return null;
    }
}