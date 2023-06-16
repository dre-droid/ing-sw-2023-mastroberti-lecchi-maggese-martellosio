package Server.Socket;

import java.net.Socket;

/**
 * This class is used as a C-style struct to store clients' socket/nickname pairs and their
 * last ping in a single list
 */
public class socketNickStruct {
    private Socket socket;
    private String name;
    private long lastPing;

    public socketNickStruct(Socket socket, String name){
        this.name = name;
        this.socket = socket;
        this.lastPing = System.currentTimeMillis();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public long getLastPing() { return lastPing;}
    public void setLastPing(long lastPing) {this.lastPing = lastPing;}
}
