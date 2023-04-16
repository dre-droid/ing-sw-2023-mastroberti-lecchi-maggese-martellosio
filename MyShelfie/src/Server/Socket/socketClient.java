package Server.Socket;

import java.net.Socket;

public class socketClient {
    private Socket socket;
    private String name;

    public socketClient(Socket socket, String name){
        this.name = name;
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }
}
