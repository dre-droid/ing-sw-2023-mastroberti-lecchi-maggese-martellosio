package Server;

import com.beust.ah.A;
import main.java.it.polimi.ingsw.Model.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Controller {
    private Game game;

    public Controller(){
    }

    public boolean createNewGame(String nickname, int numOfPlayers) throws java.rmi.RemoteException{
        if(game==null){
            game = new Game(numOfPlayers);
            game.addPlayer(nickname);
            System.out.println("Created new game by "+nickname);
            return true;
        }
        System.out.println("There is already a game to join");
        return false;
    }

    public int joinGame(String nickname) {
        if (game == null) {
            System.out.println("There is no game to join, create a new one " + nickname);
            return -1;
        }
        if (game.hasGameStarted()) {
            System.out.println("The game has alredy started, " + nickname + " can't join");
            return -2;
        }
        if (game.addPlayer(nickname)) {
            System.out.println(nickname + " joined the game");
            return 0;
        }else{
            System.out.println("Nickname already used");
            return -3;
        }
    }

    public boolean hasGameStarted(){
        if(game!=null){
            return game.hasGameStarted();
        }
        return false;
    }

    public Tile[][] getMyShelf(String playerNickname){
        if(game.hasGameStarted()){
            Tile[][] displayGrid = new Tile[6][5];
            for(Player p: game.getPlayerList()){
                if(p.getNickname().equals(playerNickname))
                    displayGrid = p.getShelf().getGridForDisplay();
            }
            return displayGrid;
        }
        return null;
    }

    public TilePlacingSpot[][] getBoard(){
        if(game.hasGameStarted()){
            return game.getBoard().getBoardForDisplay();
        }
        return null;
    }

    public boolean isMyTurn(String playerNickname){
        return game.isPlaying.getNickname().equals(playerNickname);
    }

    public String getNameOfPlayerWhoIsCurrentlyPlaying(){
        return game.isPlaying.getNickname();
    }

    /*public boolean hasGameBeenCreated(){
        return game!=null;
    }*/

    public String getCommonGoalCard1Description(){
        return game.getCommonGoalCards().get(0).getDescription();
    }

    public String getCommonGoalCard2Description(){
        return game.getCommonGoalCards().get(1).getDescription();
    }

    public int getPoints(String playerNickname) {
        return game.getPlayerList().stream().filter(player -> player.getNickname().equals(playerNickname)).toList().get(0).getScore();
    }


    public List<Tile> drawFromBoard(String playerNickname, int x, int y, int amount, Board.Direction direction){
        List<Tile> toBeReturned = new ArrayList<>();
        if(game.isPlaying.getNickname().equals(playerNickname)){
            try{
                toBeReturned = game.isPlaying.drawTiles(x,y,amount,direction);
            }catch(Exception e){
                return null;
            }
            return toBeReturned;
        }
        return null;
    }

    public boolean insertTilesInShelf(String playerNickname, List<Tile> tiles, int column){
        if(playerNickname.equals(game.isPlaying.getNickname())){
            if(column<0 || column>5)
                return false;
            try{
                if(game.insertTilesInShelf(tiles,column,game.isPlaying))
                    return true;
                else
                    return false;
            }catch(InvalidMoveException e){
                return false;
            }
        }
        return false;
    }

    public boolean checkIfCommonGoalN1IsFulfilled(String playerNickname){
        if(playerNickname.equals(game.isPlaying.getNickname())){
            return game.checkIfCommonGoalN1IsFulfilled(game.isPlaying);
        }
        return false;
    }

    public boolean checkIfCommonGoalN2IsFulfilled(String playerNickname){
        if(playerNickname.equals(game.isPlaying.getNickname())){
            return game.checkIfCommonGoalN2IsFulfilled(game.isPlaying);
        }
        return false;
    }

    public void endOfTurn(String playerNickname){
        if(playerNickname.equals(game.isPlaying.getNickname())){
            game.endOfTurn(game.isPlaying);
        }
    }

    public boolean hasTheGameEnded(){
        return game.hasTheGameEnded();
    }

    public List<Player> getLeaderboard(){
        return game.getLeaderBoard();
    }

}
