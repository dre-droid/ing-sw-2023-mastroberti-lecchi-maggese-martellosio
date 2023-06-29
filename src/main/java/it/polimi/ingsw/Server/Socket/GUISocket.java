package main.java.it.polimi.ingsw.Server.Socket;

public class GUISocket extends ClientSocket{
    public final Object connectionLostLock = new Object();

    public final Object gameEndLock = new Object();
    public final Object chatMessageLock = new Object();
    public final Object drawLock = new Object();
    public final Object nextSceneLock = new Object();
    public final Object turnHasEndedLock = new Object();
    public final Object insertLock = new Object();
    public final Object endGameTokenNickLock = new Object();
    public String endGameTokenNick = "";
    public boolean insert = false;
    public boolean gameEnd = false;
    public boolean draw = false;
    public String nextScene = "";
    public GUISocket(String ip){
        super(ip);
    }

    /**
     * This method is called to handle the request coming from the server to
     * perform all the operations carried out during the turn.
     * @param line is the message received from the server
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
            if(line.equals("[EXIT]")){
                //System.exit(0);
            }

        }
        if (line.startsWith("[INFO] Game is starting.")){
            synchronized (nextSceneLock) {
                nextScene = "GameStart";
                turnOfPlayer = line.replace("[INFO]: Game is starting. ", "");
                nextSceneLock.notifyAll();
            }
        }
        if (line.startsWith("[REQUEST] Invalid nickname") || line.startsWith("[REQUEST] Nickname already in use")){
            synchronized (nextSceneLock) {
                line = line.replace("[REQUEST] ", "");
                nextScene = line;
                nextSceneLock.notifyAll();
            }
        }
        if (line.startsWith("[REQUEST] Choose the number of players for the game:")){
            synchronized (nextSceneLock) {
                nextScene = "MatchType";
                nextSceneLock.notifyAll();
            }
        }
        if (line.startsWith("[INFO] Waiting for all players to connect...") || line.startsWith("[INFO] You have successfully rejoined the game")
                || line.startsWith("[INFO] Joined a Game")){
            synchronized (nextSceneLock) {
                nextScene = "GameScene";
                nextSceneLock.notifyAll();
            }
        }
        if (line.equals("[INFO] Game is being created by another player...") || line.startsWith("[INFO] The game already started")){
            synchronized (nextSceneLock) {
                nextScene = line.replace("[INFO] ", "");
                nextSceneLock.notifyAll();
            }
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
        if(line.startsWith("[GAMEEND]")){
            synchronized (gameEndLock){
                gameEnd = true;
                gameEndLock.notifyAll();
            }
        }
        if(line.startsWith("[CURRENTPLAYER]")){
            synchronized (turnHasEndedLock){
                turnHasEnded = true;
                turnHasEndedLock.notifyAll();
            }
        }
        if(line.startsWith("[ENDGAMETOKEN]")){
            synchronized (endGameTokenNickLock){
                System.out.println(line);
                endGameTokenNick = line.replace("[ENDGAMETOKEN] ", "");
                endGameTokenNickLock.notifyAll();
            }
        }

    }

    /**
     * This method is used to warn socketUpdateGameScene() in GameSceneController that connection to the server has been lost
     */
    protected void disconnectionAlert(){
        synchronized (connectionLostLock){
            connectionLost = true;
            connectionLostLock.notifyAll();
            try{
                Thread.sleep(5000);
            }catch (Exception e){
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}
