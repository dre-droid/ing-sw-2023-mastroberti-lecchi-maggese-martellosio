package GUI;

import Server.ClientWithChoice;
import Server.Socket.ClientSocket;

public class GUIServer {
    //Socket
    public void createClient() {
        ClientWithChoice client = new ClientWithChoice();
        client.run();
    }


}
