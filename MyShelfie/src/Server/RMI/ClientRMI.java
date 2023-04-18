package Server.RMI;

import java.rmi.RemoteException;

public class ClientRMI {

    public static void main(String[] args){
        try {
            ClientNotificationRMI client1 = new ClientNotificationRMI();
            Thread thread = new Thread(client1);
            thread.start();

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
