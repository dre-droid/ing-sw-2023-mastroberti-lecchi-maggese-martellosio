package Server;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientRMI {

    public static void main(String[] args){
        try{

            Registry registryServer = LocateRegistry.getRegistry();
            RMIinterface serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");

            Scanner userInput = new Scanner(System.in);
            System.out.println("Insert the port number: ");
            int myport = Integer.parseInt(userInput.nextLine());
            ClientNotificationRMI notifications = new ClientNotificationRMI();
            Registry registryNotifications = LocateRegistry.createRegistry(myport);
            registryNotifications.rebind("Client",notifications);
            System.out.println("Now insert the nickname for the game: ");
            int returnCode;
            do{
                String nickname = userInput.nextLine();
                returnCode = serverRMI.joinGame(nickname,myport);
                switch(returnCode){
                    case -1: {
                        System.out.println("Creating a new game...How many players can join your game?");
                        int numPlayers = Integer.parseInt(userInput.nextLine());
                        if(serverRMI.createNewGame(nickname,numPlayers,myport)){
                            returnCode=0;
                        }
                        else{
                            System.out.println("Somebody already created the game, try to join again");
                        }
                    }break;
                    case -2:{
                        System.out.println("Try again later");
                    }break;
                    case -3:{
                        System.out.println("Try a different nickname");
                    }break;
                    default:;
                }
            }while(returnCode!=0);


            //serverRMI.joinGame(nickname,myport);



        }catch(RemoteException | NotBoundException e){
            e.printStackTrace();
        }
    }
}
