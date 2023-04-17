package Server.Socket;

import java.net.Socket;

/**
 * this class is used as a C-style struct to store clients' socket/nickname pairs in a single list
 */
public class socketNickStruct {
    private Socket socket;
    private String name;

    public socketNickStruct(Socket socket, String name){
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
