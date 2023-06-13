package Server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientInfoStruct {
    private String nickname;
    private Socket socket = null;

    private PrintWriter out;
    private BufferedReader reader;
    private int rmiPort = 0;
    public ClientInfoStruct(String nickname){
        this.nickname = nickname;
    }
    public String getNickname() { return nickname;}

    public Socket getSocket() {
        return socket;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public BufferedReader getReader() {
        return reader;
    }

}
