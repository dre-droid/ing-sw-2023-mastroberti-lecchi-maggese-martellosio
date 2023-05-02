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
    private Server server;
    private Thread runServer, acceptClient;
    private List<Thread> clientListeners;
    public String string = "";
    private String quitter;
    public boolean isFirstTurn;
    public int nTry;

    public ServerSock(Controller controller, Server server){
        this.controller = controller;
        this.server = server;
    }

    /**
     * Creates a thread to accept clients.
     */
    public void runServer(){
        ServerSocket serverSocket = null;
        clientListeners = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(59010);

            System.out.println("Socket server up and running...");
        }catch (IOException e) {e.printStackTrace();}

        ServerSocket finalServerSocket = serverSocket;
        runServer = new Thread(() -> {
            while (true){
            try {
                    Socket client = finalServerSocket.accept();
                    acceptClient(client);

                }
                catch (IOException e) {
                    System.out.println("Ded");  //lol
                }
            }
        });
        runServer.start();
    }

    /**
     * Creates thread to let a client join the game (thread allows multiple connections simultaneously). Adds client's socket to
     * List<socketNickStruct> clients if successful. Also creates a clientListener thread for each client.
     * @param client
     */
    private void acceptClient(Socket client) {
        acceptClient = new Thread(() -> {
            try {
                boolean repeat = true;
                while (repeat){
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("[INFO]: Welcome to MyShelfie! Press 'q' to quit.");
                    int resultValue = playerJoin(client);

                    if (resultValue == -3) repeat = true;   //invalid nickname
                    else if (resultValue == 0|| resultValue == -1) {    //successfully joined
                        repeat = false;
                        clientListener(client, getNickFromSocket(client));
                    }
                };    //while nickname is not valid, keep trying for a new name
            }
            catch (SocketException s) {
                System.out.println("You quit.");
            }
            catch (IOException e) {
                try {client.close();}
                catch (IOException ex) {ex.printStackTrace();}
                finally {System.out.println("Client failed to join");}
            }
        });
        acceptClient.start();
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
                    if (counter > 0) out.println("[INFO]: Input non valido. Riprova inserendo un numero da 2 a 4.");
                    out.println("[REQUEST] Inserisci il numero di giocatori: ");
                    line = reader.readLine();
                    System.out.println("Number of players selected: " + line);
                    counter++;
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
     * creates a thread to listen to the clients' messages. If client's turn, writes clients messages to global variable string. If message starts with
     * '/c' processes message for chat, otherwise output is ignored.
     * @param client
     * @param nickname
     */
    public void clientListener(Socket client, String nickname){
        Thread clientListener = new Thread(() -> {
            try {
                String line = "";
                InputStream input = client.getInputStream();
                boolean active = true;
                while (active) {
                    Thread.sleep(50);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    line = reader.readLine();

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
                        System.out.println("chiamando chat"); //chiamata a chat

                    if (line.equals("/quit")) {//game quit
                        //if (!controller.hasGameBeenCreated()) client.close();   //not right way of handling
                        //else {
                            controller.endGame();
                            for (socketNickStruct c : clients) {
                                PrintWriter pw = new PrintWriter(c.getSocket().getOutputStream(), true);
                                pw.println("[GAMEEND]: " + nickname + " has quit the game. Game has ended.");
                                c.getSocket().close();
                            }
                        //}

                    }

                }
            } catch (SocketException e){
                System.out.println(nickname + "'s socket has been closed.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
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
    public drawInfo drawInquiry(String nickname, Board b, Shelf shelf, PersonalGoalCard pgc, List<CommonGoalCard> cgc, List<Player> leaderboard){
        Socket playerSocket = null;
        drawInfo drawInfo = new drawInfo();

        //find client's socket
        for (socketNickStruct c: clients)
            if (c.getName().equals(nickname)){
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
            for (Player p: leaderboard) stringLeaderboard.add(p.getNickname() + ": " + p.getScore());
            String jsonLeaderboard = gson.toJson(stringLeaderboard);
            out.println("[GSONLEAD]" + jsonLeaderboard);
            //*********************************************

            boolean imbecille = false;
            String line;

            //asks for row
            do {
                Thread.sleep(50);
                if (!controller.hasTheGameEnded()) {
                    if (imbecille)
                        out.println("[REQUEST] Invalid Input! Select the row from which to draw from:");
                    else
                        out.println("[YOUR TURN] Select the row from which to draw from:");
                }
                while(Objects.equals(string, "")){
                    if (controller.hasTheGameEnded()) break;
                    Thread.sleep(100);
                }
                line = string;
                string = "";
                if (line.equals("q") || controller.hasTheGameEnded()) return null;
                imbecille = true;
                System.out.println("PRINT DIOC");
            }while(!isNumeric(line) || Integer.parseInt(line)>8 || Integer.parseInt(line)<0);
            drawInfo.setX(Integer.parseInt(line));
            imbecille = false;

            //asks for column
            do {
                if(imbecille)
                    out.println("[REQUEST] Invalid Input! Select the column from which to draw from:");
                else
                    out.println("[REQUEST] Select the column from which to draw from:");
                while(Objects.equals(string, "")){
                    if (controller.hasTheGameEnded()) break;
                    Thread.sleep(100);
                }
                line = string;
                string = "";
                if (line.equals("q") || controller.hasTheGameEnded()) return null;
                imbecille = true;
            }while(!isNumeric(line) || Integer.parseInt(line)>8 || Integer.parseInt(line)<0);
            drawInfo.setY(Integer.parseInt(line));
            imbecille = false;

            //asks for tile quantity to be drawn
            do {
                if(imbecille)
                    out.println("[REQUEST] Invalid Input! How many tiles do you want to draw?");
                else
                    out.println("[REQUEST] How many tiles do you want to draw?");
                while(Objects.equals(string, "")){
                    if (controller.hasTheGameEnded()) break;
                    Thread.sleep(100);
                }
                line = string;
                string = "";
                if (line.equals("q") || controller.hasTheGameEnded()) return null;
                imbecille = true;
            }while(!isNumeric(line));
            drawInfo.setAmount(Integer.parseInt(line));
            imbecille = false;

            //asks for tile direction
            do {
                if(imbecille)
                    out.println("[REQUEST] Invalid Input! In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                else
                    out.println("[REQUEST] In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)");
                while(Objects.equals(string, "")){
                    if (controller.hasTheGameEnded()) break;
                    Thread.sleep(100);
                }
                line = string;
                string = "";
                if (line.equals("q") || controller.hasTheGameEnded()) return null;
                imbecille = true;
            }while(!isNumeric(line) || Integer.parseInt(line)>3 || Integer.parseInt(line)<0);
            drawInfo.setDirection(Board.Direction.values()[Integer.parseInt(line)]);
            imbecille = false;

            List<Tile> drawnTiles;
            TilePlacingSpot[][] grid = b.getBoardForDisplay();
            drawnTiles = b.getTilesForView(drawInfo.getX(), drawInfo.getY(), drawInfo.getAmount(), drawInfo.getDirection());

            String stringa = "[INFO]: Here are your tiles: ";
            int i = 1;
            for (Tile t: drawnTiles){
                stringa += i + ")" + t + " " ;
                i++;
            }
            out.println(stringa);

            out.println("[SHELF] Here is your Shelf: ");
            jsonShelf = gson.toJson(shelf);
            out.println("[GSONSHELF]" + jsonShelf);

            do {
                if(imbecille)
                    out.println("[REQUEST] Invalid Input! Choose in which column you want to insert the tiles: [0 ... 4]");
                else
                    out.println("[REQUEST] Choose in which column you want to insert the tiles: [0 ... 4]");
                while(Objects.equals(string, "")){
                    if (controller.hasTheGameEnded()) break;
                    Thread.sleep(100);
                }
                line = string;
                string = "";
                if (line.equals("q") || controller.hasTheGameEnded()) return null;
                imbecille = true;
            }while(!isNumeric(line) || Integer.parseInt(line)>4 || Integer.parseInt(line)<0);
            drawInfo.setColumn(Integer.parseInt(line));
            imbecille = false;

            if(drawInfo.getAmount() != 1) {
                do {
                    if (imbecille)
                        out.println("[REQUEST] Invalid Input! Choose in which order you want to insert the tiles: [e.g. CGT -> TCG: 312]");
                    else
                        out.println("[REQUEST] Now choose in which order you want to insert the tiles: [e.g. CGT -> TCG: 312]");
                    imbecille = false;
                    while(Objects.equals(string, "")){
                        if (controller.hasTheGameEnded()) break;
                        Thread.sleep(50);
                    }
                    line = string;
                    string = "";
                    if (line.equals("q") || controller.hasTheGameEnded()) return null;
                    if (!isNumeric(line))
                        imbecille = true;
                    else {
                        if (drawInfo.getAmount() == 2) {
                            if (!Objects.equals(line, "12") && !Objects.equals(line, "21"))
                                imbecille = true;
                        }
                        if (drawInfo.getAmount() == 3) {
                            if (!Objects.equals(line, "123") && !Objects.equals(line, "132") && !Objects.equals(line, "213") && !Objects.equals(line, "231") && !Objects.equals(line, "312") && !Objects.equals(line, "321"))
                                imbecille = true;
                        }
                    }

                } while (imbecille || !controller.hasTheGameEnded());
                drawInfo.setOrder(Integer.parseInt(line));
            }

        } catch(SocketException e){
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();};

        return drawInfo;
    }

    /**
     * This method is used to update the client on their turn - it informs the turn has succesfully ended and shows them their updated shelf
     * @param updatedShelf
     * @param nickname
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

    /*
    /**
     * notifies all socket players of game ending
     * @param nick - the nickname of the player who quit or disconnected
     * @throws IOException
     */
    public void notifyGameEnd(String nick) throws IOException {
        //find client's socket
        for (socketNickStruct c: clients){
            PrintWriter out = new PrintWriter(c.getSocket().getOutputStream(), true);
            out.println("[GAMEEND]: " + nick + " has quit the game. The game has ended.");
            c.getSocket().close();
        }
    }
    /*

    /**
     *  Creates new instance of clients array
     */
    public void flushServer(){
            clients = new ArrayList<>();
    }

    public void setController(Controller c){ this.controller = c;}

    public void setQuitter(String s){ quitter = s;}

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
}