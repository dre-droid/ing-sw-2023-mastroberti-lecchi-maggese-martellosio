package Server.RMI;

/**
 * This class is used as a C-style struct to store clients' nickname and their
 * last ping in a single list
 */
public class RmiNickStruct{
    private String nickname;
    private long lastPing;
    public RmiNickStruct(String nickname){
        this.nickname = nickname;
        this.lastPing = System.currentTimeMillis();
    }
    public String getNickname() { return nickname;}
    public long getLastPing() { return lastPing;}
    public void setLastPing(long lastPing) {this.lastPing = lastPing;}
}