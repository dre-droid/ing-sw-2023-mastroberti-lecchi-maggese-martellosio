package Server;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientRMI {

    public static void main(String[] args){
        try{

            Registry registry = LocateRegistry.getRegistry();
            RMIinterface serverRMI = (RMIinterface) registry.lookup("MyShelfie");
            String nickname = "Giuseppino";
            serverRMI.createNewGame("Giuseppino",3);
            serverRMI.joinGame("Mario321");
        }catch(RemoteException | NotBoundException e){
            e.printStackTrace();
        }
    }
}
