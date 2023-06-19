package Server.Socket;

public class GUISocket extends ClientSocket{
    /**
     * Handles server's received messages
     */
    protected synchronized void handleServerRequest(String line){
        if (line.equals("[CONNECTED]")) {
            serverPinger();
        }
        if (line.startsWith("[INFO] Chosen nickname:")){
            nickname = line.replace("[INFO] Chosen nickname: ", "");
        }
        if(line.equals("[EXIT]")){
            System.exit(0);
        }
        if (line.startsWith("[YOUR TURN]")) {
            System.out.println(line);
        }
        if (line.startsWith("[INVALID MOVE]")) {
            System.out.println("You cannot select those tiles. Try again.\n");
        }
        if (line.startsWith("[REQUEST]") || line.startsWith("[MESSAGE") || line.startsWith("[INFO]")) {
            System.out.println(line);
        }
        if (line.startsWith("[SHELF]")) {
            System.out.println(line);
        }
        if (line.startsWith("[TURNEND]")) {
            System.out.println();
            System.out.println(line);
            System.out.println("******************************");
        }
        if (line.startsWith("[GAMEEND]")) {
            System.out.println(line);
            System.exit(0);
        }




        if (line.equals("[CONNECTED]")) {
            serverPinger();
        }
        if (line.startsWith("[INFO] Chosen nickname:")){
            nickname = line.replace("[INFO] Chosen nickname: ", "");
        }
        if (line.startsWith("[REQUEST] Invalid nickname.") || line.startsWith("[REQUEST] Nickame already in use")){
            line = line.replace("[REQUEST] ", "");
            nextScene = line.replace("[INFO] ", "");
            System.out.println("client socket " + nextScene);
        }
        else if (line.startsWith("[REQUEST] Choose the number of players for the game:")){
            nextScene = "MatchType";
        }
        else if (line.startsWith("[INFO] Game is being created by another player") || line.startsWith("[INFO] Waiting for all players to connect...")) {
            nextScene = "GameScene";
        }
        else if (line.startsWith("[MESSAGE FROM")){
            chatMessage = line;
        }
        if (line.startsWith("[INFO] Game is starting.")){
            nextScene = "GameStart";
            turnOfPlayer = line.replace("[INFO]: Game is starting. ", "");
        }
        notify();
    }
}
