package Server;

import Server.RMI.ClientNotificationRMI;
import Server.RMI.ClientRMI;
import Server.Socket.CLISocket;
import Server.Socket.ClientSocket;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
            }
            if(connectionType<1 || connectionType>2)
                System.out.println("Insert 1 for RMI, 2 for Socket");
        }while(connectionType<1 || connectionType>2);

        if(connectionType==1){
            //RMI CLIENT
            createNewRMIClient();
        }
        else{
            //SOCKET CLIENT
            System.out.println("First of all insert the ip address of the server:");
            String serverIp;
            boolean connected = false;
            do {
                serverIp = userInput.nextLine();
                try{
                    InetAddress inetAddress = InetAddress.getByName(serverIp);
                    if(inetAddress instanceof Inet4Address){
                        if(serverIp.equals(inetAddress.getHostAddress())) {
                            if (serverIp.equals("127.0.0.2")) {
                                connected = false;
                            } else {
                                System.out.println("correct ip: " + serverIp);
                                connected=true;
                            }
                        }
                        else{
                            connected = false;
                        }
                    }
                    else{
                        connected=false;
                    }
                }catch(UnknownHostException uhe){
                    connected = false;
                }
                if(!connected){
                    System.out.println("The ip you used is not a correct ip or it does not correspond to the server ip");
                }
            }while(!connected);

            createNewSocketClient(serverIp);
        }

    }

    public void createNewRMIClient(){
        ClientRMI clientRMI = new ClientRMI();
        Thread thread = new Thread(clientRMI);
        thread.start();
    }

    public void createNewSocketClient(String ip){
        ClientSocket clientSocket = new CLISocket(ip);
        clientSocket.runServer();
    }

    public static void main(String[] args){
        ClientWithChoice client = new ClientWithChoice();
        client.run();
    }
}
