package Server.Socket;

import main.java.it.polimi.ingsw.Model.Game;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Server {
    static ServerSocket serverSocket;
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();
    private static final ArrayList<Game> game = new ArrayList<>();
    private static boolean active;


    public static void main(String[] args) throws IOException {
        //server creation
        serverSocket = new ServerSocket(1234);
        System.out.println("Waiting for client connection...");
        active = true;

        try {
            while (active) {
                Socket client = serverSocket.accept();
                acceptClient(client);
            }
        } catch (IOException e) {
            System.out.println("Ded");
        }
    }

    private static void acceptClient(Socket client) {
        Runnable acceptClient = () -> {
            try {
                playerJoin(client);
            } catch (IOException e) {

                try {
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.out.println("Client failed to join");
            }
        };

        new Thread(acceptClient).start();
    }

    private static void playerJoin(Socket client) throws IOException {
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
        out.println("[REQUEST] Inserisci un nickname:");
        String line = reader.readLine();
        System.out.println(line);
        out.println("Il nickname inserito è:  "+ line);
        out.println("[REQUEST] Inserisci il numero di giocatori: ");
        line = reader.readLine();
        System.out.println(line);
        out.println("Il numero di giocatori inserito è:  "+ line);



    }


}