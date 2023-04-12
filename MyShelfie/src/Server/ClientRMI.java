package Server;

import com.beust.ah.A;
import main.java.it.polimi.ingsw.Model.*;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
