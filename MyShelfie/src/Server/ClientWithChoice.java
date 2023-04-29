package Server;

import Server.RMI.ClientNotificationRMI;
import Server.RMI.ClientRMI;
import Server.Socket.ClientSocket;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Scanner;

public class ClientWithChoice {

    public void run(){
        Scanner userInput = new Scanner(System.in);
        System.out.println("[CONNECTION-CHOICE] Input 1 if you want to connect to RMI server, 2 if you want to use the socket");
        int connectionType=0;
        do{
            try{
                connectionType = Integer.parseInt(userInput.nextLine());
            }catch(Exception e){
                System.out.println("Insert 1 for RMI, 2 for Socket");
            }
            if(connectionType<1 || connectionType>2)
                System.out.println("Insert 1 for RMI, 2 for Socket");
        }while(connectionType<1 || connectionType>2);

        if(connectionType==1){
            //RMI CLIENT
            ClientRMI clientRMI = new ClientRMI();
            Thread thread = new Thread(clientRMI);
            thread.start();
        }
        else{
            //SOCKET CLIENT
            ClientSocket clientSocket = new ClientSocket();
            clientSocket.runServer();
        }

    }

    public static void main(String[] args){
        ClientWithChoice client = new ClientWithChoice();
        client.run();
    }
}
