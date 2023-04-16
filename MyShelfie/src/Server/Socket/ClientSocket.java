package Server.Socket;


import java.net.Socket;
import java.util.Scanner;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClientSocket {
    public static void main(String[] args) throws IOException {
        //System.out.println("Insert the port number: ");
        //BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        //String s = bufferRead.readLine();

        try{
            //connect to server
            Socket socket= new Socket("127.0.0.1", 59010);
            socket.setKeepAlive(true);

            try{
                serverListener(socket);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {throw new RuntimeException(e);}

        }


    private static void serverListener(Socket socket) {
        Runnable serverListener = () -> {
            try {
                InputStream input = socket.getInputStream();
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                String message;
                boolean active = true;

                while(active){
                    line = reader.readLine();
                    if( line != null){
                        System.out.println(line);
                        if(line.startsWith("[REQUEST]")){
                            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                            message = bufferRead.readLine();
                            output.println(message);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        new Thread(serverListener).start();
    }




}


