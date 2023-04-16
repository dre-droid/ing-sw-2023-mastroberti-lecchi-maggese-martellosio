package Server.Socket;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ServerSock {

    private final ArrayList<Socket> clients = new ArrayList<>();
    private Server.Controller controller;
    //private static final ArrayList<Game> game = new ArrayList<>();


    public ServerSock(Server.Controller controller){
        this.controller = controller;
    }

    public void runServer(){
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
                        clients.add(client);
                        repeat = false;
                    }
                };    //while nickname is not valid, keep trying for a new name
                clients.add(client);
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
                return 0;
            }
        }
        return -4;  //should never reach!
    }

}