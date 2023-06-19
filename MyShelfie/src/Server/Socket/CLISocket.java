package Server.Socket;

import main.java.it.polimi.ingsw.Model.Player;
import main.java.it.polimi.ingsw.Model.Shelf;
import java.util.Scanner;

public class CLISocket extends ClientSocket{
    /**
     * Handles server's received messages
     */
     protected synchronized void handleServerRequest(String line){
        if (line.equals("[CONNECTED]")) {
            serverPinger();
            disconnectionCheck();
        }
         if (line.startsWith("[INFO] Chosen nickname:")){
             nickname = line.replace("[INFO] Chosen nickname: ", "");
         }
         if(line.equals("[EXIT]")){
            System.exit(0);
        }
         if (line.startsWith("[YOUR TURN]")) {
            printTurn();
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
            printShelf(getShelf());
        }
        if (line.startsWith("[TURNEND]")) {
            printShelf(getShelf());
            System.out.println();
            System.out.println(line);
            System.out.println("******************************");
        }
        if (line.startsWith("[GAMEEND]")) {
            System.out.println(line);
            System.exit(0);
        }
    }

    /**
     * Prints a command line view of the player's turn
     */
    private void printTurn(){
        System.out.println();
        System.out.println("*********  " + nickname + ": your turn  *********");

        //shelf & personalGoal print
        Scanner scannerpg = new Scanner(personalGoalCard.toString());
        Scanner scannercg = new Scanner(commonGoalCards.get(0).getDescription() + "\n" + commonGoalCards.get(1).getDescription());

        System.out.println("*** Shelf ***  *** Personal Goal Card ***  *** Common Goal Card ***");
        for (int i = 0; i < 6; i++) {
            System.out.print("   ");
            for (int j = 0; j < 5; j++){
                if (shelf.getGrid()[i][j] == null) System.out.print("x ");
                else System.out.printf("%s ", shelf.getGrid()[i][j].toString());
            }
            System.out.print("   ");
            System.out.print(scannerpg.nextLine());
            System.out.print("   ");
            if (scannercg.hasNextLine()) System.out.print(scannercg.nextLine());
            System.out.println();
        }
        System.out.println();

        //board print
        board.printGridMap();
        System.out.println();

        //leaderboard print
        System.out.println("Leaderboard");
        int i = 0;
        for (Player p: leaderboard) {
            System.out.print(i + 1 + ". " + p.getNickname() + ": ");
            System.out.println(p.getScore());
            i++;
        }
        System.out.println();

        //print other player shelfs
        for(Player p: leaderboard){
            if(!p.getNickname().equals(getNickname())){
                System.out.println(p.getNickname()+"'s Shelf");
                printShelf(p.getShelf());

            }
        }
    }

    private void printShelf(Shelf shelf){
        System.out.println("*** Shelf ***");
        System.out.println("1 2 3 4 5");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (shelf.getGrid()[i][j] == null) System.out.print("x ");
                else System.out.printf("%s ", shelf.getGrid()[i][j].toString());
            }
            System.out.println();
        }
        System.out.println("*************");
    }
    protected void disconnectionAlert(){
        System.out.println("Connection lost, try again later");
    }
}
