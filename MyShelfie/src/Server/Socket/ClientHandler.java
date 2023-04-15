package Server.Socket;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import com.google.gson.Gson;

public class ClientHandler implements Runnable{
    private String nickname;
    private Socket client;
    private InputStream in;
    private PrintWriter out;
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();

    public ClientHandler(Socket clientSocket) throws IOException{
        this.client= clientSocket;
        this.in = clientSocket.getInputStream();
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        clients.add(this);

    }
    public void run(){
        System.out.println("hi");
    }
}
