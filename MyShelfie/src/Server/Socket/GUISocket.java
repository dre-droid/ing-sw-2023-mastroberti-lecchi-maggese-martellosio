package Server.Socket;

public class GUISocket extends ClientSocket{
    public final Object chatMessageLock = new Object();
    public final Object drawLock = new Object();
    public boolean draw = false;
    public final Object insertLock = new Object();
    public boolean insert = false;
    public GUISocket(String ip){
        this.ip = ip;
    }
    /**
     * Handles server's received messages
     */
    protected void handleServerRequest(String line){
        synchronized (this){
            // temporary CLI for debugging
            if (line.startsWith("[YOUR TURN]")) {
                //printTurn();
                System.out.println(line);
            }
            if (line.startsWith("[INVALID MOVE]")) {
                System.out.println("You cannot select those tiles. Try again.\n");
            }
            if (line.startsWith("[REQUEST]") || line.startsWith("[MESSAGE") || line.startsWith("[INFO]")) {
                System.out.println(line);
            }
            if (line.startsWith("[INFO]: Game is starting.")){
                //if (!isPlaying.equals(nickname)) printTurn(isPlaying);
            }
            if (line.startsWith("[SHELF]")) {
                System.out.println(line);
                //printShelf(getShelf());
            }
            if (line.startsWith("[TURNEND]")) {
                //printShelf(getShelf());
                System.out.println();
                System.out.println(line);
                System.out.println("******************************");
            }
            if (line.startsWith("[GAMEEND]")) {
                System.out.println(line);
                //printLeaderboard();
                gameEnd = true;
                //System.exit(0);
            }
            if(line.equals("[EXIT]")){
                System.exit(0);
            }



            //GUI
            if (line.startsWith("[REQUEST] Invalid nickname.") || line.startsWith("[REQUEST] Nickname already in use")){
                line = line.replace("[REQUEST] ", "");
                nextScene = line;
                System.out.println("client socket " + nextScene);
            }
            else if (line.startsWith("[REQUEST] Choose the number of players for the game:")){
                nextScene = "MatchType";
            }
            else if (line.startsWith("[INFO] Waiting for all players to connect...") || line.startsWith("[INFO] You have successfully rejoined the game")
            || line.equals("[INFO] Joined a Game")) {
                nextScene = "GameScene";
            }
            if(line.equals("[INFO] Game is being created by another player...") || line.startsWith("[INFO] The game already started")){
                nextScene = line.replace("[INFO] ", "");
            }
            if (line.startsWith("[INFO] Game is starting.")){
                nextScene = "GameStart";
                turnOfPlayer = line.replace("[INFO]: Game is starting. ", "");
            }
        notifyAll();
        }
        if (line.startsWith("[MESSAGE FROM")){
            synchronized (chatMessageLock){
                chatMessage = line;
                chatMessageLock.notifyAll();
            }
        }
        if (line.equals("[YOUR TURN] Select the row from which to draw from:") || line.equals("[REQUEST] How many tiles do you want to draw?") ||
                line.equals("[REQUEST] Select the column from which to draw from:") || line.equals("[REQUEST] In which direction? (0=UP, 1=DOWN, 2=RIGHT, 3=LEFT)")){
            synchronized (drawLock){
                draw = true;
                drawLock.notifyAll();
            }
        }
        if(line.startsWith("[REQUEST]: Choose which tile to") || line.equals("[REQUEST] Choose in which column you want to insert the tiles: [1 ... 5]")){
            synchronized (insertLock){
                insert = true;
                insertLock.notifyAll();
            }
        }
        if(line.startsWith("[INFO] You have successfully rejoined the game")){
            synchronized (chatMessageLock){
                chatMessage = "You have successfully rejoined the game";
                chatMessageLock.notifyAll();
            }
        }
    }
    protected void disconnectionAlert(){
        synchronized (chatMessageLock){
            chatMessage = "Connection lost, try again later";
            chatMessageLock.notifyAll();
        }
    }
}
