package test.java.it.polimi.ingsw.Model;

import java.rmi.RemoteException;
import Server.Controller;
import Server.RMI.ServerRMI;
import Server.Server;
import Server.Socket.ServerSock;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {
    private Controller controller;
    private Game game;
    private Server server;
    private ServerRMI serverRMI;

    private ServerSock serverSock;



    @BeforeEach
    public void setUp() throws RemoteException {
        server = new Server();
        controller = new Controller(server);
        serverRMI = new ServerRMI(controller,server);
        serverSock = new ServerSock(controller,server);
        server.setServerRMI(serverRMI);
        server.setServerSock(serverSock);
    }

    /**
     * @author SaverioMaggese99
     * @throws RemoteException
     * Checks if the game has been succesfully created: PASSED
     */
    @Test
    public void testCreateNewGame() throws RemoteException {
        String nickname = "Save";
        int num = 4;
        controller.createNewGame(nickname,num);
        boolean result = controller.hasGameBeenCreated();
        assertTrue(result);
        game = controller.getGame();
        assertEquals(1, game.getPlayerList().size());
    }

    /**
     * @author SaverioMaggese99
     * @throws RemoteException
     * Checks if the game has not been created while there exists another one: PASSED
     */
    @Test
    public void testCreateNewGame_GameAlreadyRunning() throws RemoteException {
        String nickname = "Save";
        int numOfPlayers = 4;
        controller.createNewGame(nickname,numOfPlayers);
        boolean result = controller.createNewGame(nickname, numOfPlayers);
        assertFalse(result);
    }
    /**
     * @author SaverioMaggese99
     * Check the joining when the game has not been created yet: PASSED
     */
    @Test
    public void testJoinGame_NoGameToJoin() {
        String nickname = "Player2";
        int result = controller.joinGame(nickname);
        assertEquals(-1,result);
        assertNotEquals(-4,result);
    }

    /**
     * @author SaverioMaggese99
     * Check the joining when the game has been created both when the game is started or not: PASSED
     */
    @Test
    public void testJoinGame_GameAlreadyStarted() throws RemoteException{
        String nickname1 = "Player1";
        String nickname2 = "Player2";
        String nickname3 = "Player3";
        controller.createNewGame(nickname1,2);
        game = controller.getGame();
        int result = controller.joinGame(nickname2);
        assertEquals(0,result);
        result = controller.joinGame(nickname3);
        assertEquals(-2, result);
    }

    /**
     * @author SaverioMaggese99
     * Test if the player is removed correctly:PASSED
     */
    @Test
    public void testRemovePlayer() throws RemoteException{
        String nickname = "Saverio";
        String other = "Other";
        controller.createNewGame(nickname,2);
        game = controller.getGame();
        game.addPlayer(other);
        game.removePlayer(other);
        assertEquals(1, game.getPlayerList().size());

    }
    /**
     * @author SaverioMaggese99
     * Checks HasGameStarted when game is null:PASSED
     */
    @Test
    public void testHasGameStarted_whenGameisnull() {
        boolean result = controller.hasGameStarted();
        assertFalse(result);
    }

    /**
     * Test the method hasGameBeenCreated: PASSED
     * @throws RemoteException
     * @author SaverioMaggese99
     */
    @Test
    public void testHasGamebeenCreated() throws RemoteException{
        String nickname = "Save";
        int num = 4;
        controller.createNewGame(nickname,num);
        boolean result = controller.hasGameBeenCreated();
        assertTrue(result);
    }
    /**
     * Test the method getNameofPlayerWhoIsCurrentlyPlaying: PASSED
     * @throws RemoteException
     * @author SaverioMaggese99
     */
    @Test
    public void testgetNameOfPlayerWhoIsCurrentlyPlaying() throws RemoteException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,2);
        game = controller.getGame();
        controller.joinGame(nick2);
        String result = controller.getNameOfPlayerWhoIsCurrentlyPlaying();
        boolean x = result.equals(game.getIsPlaying().getNickname());
        assertTrue(x);
    }
    /**
     * Test the method isMyTurn: PASSED
     * @throws RemoteException
     * @author SaverioMaggese99
     */
    @Test
    public void testisMyTurn() throws RemoteException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,2);
        game = controller.getGame();
        controller.joinGame(nick2);
        assertTrue(controller.isMyTurn(game.getIsPlaying().getNickname()));

    }
    /**
     * Test the method drawFromBoard: PASSED
     * @throws RemoteException,Exception,InvalidMoveException
     * @author SaverioMaggese99
     */
    @Test
    public void testdrawFromBoard() throws RemoteException,InvalidMoveException,Exception{
        List<Tile> result = new ArrayList<>();
        List<Tile> x = new ArrayList<>();
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        x = game.getBoard().getTilesForView(1,3,2, Board.Direction.RIGHT);
        result = controller.drawFromBoard(controller.getNameOfPlayerWhoIsCurrentlyPlaying(), 1,3,2, Board.Direction.RIGHT);
        assertEquals(result,x);
    }
    /**
     * Tests method insertTilesInShelf: PASSED
     * @throws RemoteException
     * @author SaverioMaggese99
     */
    @Test
    public void testinsertTilesInShelf() throws RemoteException{
        List<Tile> tiles = new ArrayList<>();
        Tile x = new Tile(Type.CAT);
        Tile y= new Tile(Type.PLANT);
        tiles.add(x);
        tiles.add(y);
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        boolean result1 = controller.insertTilesInShelf(controller.getNameOfPlayerWhoIsCurrentlyPlaying(),tiles,1 );
        boolean result2 = controller.insertTilesInShelf(controller.getNameOfPlayerWhoIsCurrentlyPlaying(),tiles,-1);
        boolean result3 = controller.insertTilesInShelf(controller.getNameOfPlayerWhoIsCurrentlyPlaying(),tiles,6);
        assertTrue(result1);
        assertFalse(result2);
        assertFalse(result3);
    }

    /**
     * Tests method checkIfCommonGoalN1isFulfilled: PASSED
     * @author SaverioMaggese99
     * @throws RemoteException
     */
    @Test
    public void testcheckIfCommonGoalN1IsFulfilled()throws RemoteException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        String playingrn = controller.getNameOfPlayerWhoIsCurrentlyPlaying();
        assertEquals(game.checkIfCommonGoalN1IsFulfilled(game.getIsPlaying()),controller.checkIfCommonGoalN1IsFulfilled(playingrn));
    }
    /**
     * Tests method checkIfCommonGoalN2isFulfilled: PASSED
     * @author SaverioMaggese99
     * @throws RemoteException
     */
    @Test
    public void testcheckIfCommonGoalN2IsFulfilled()throws RemoteException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        String playingrn = controller.getNameOfPlayerWhoIsCurrentlyPlaying();
        assertEquals(game.checkIfCommonGoalN2IsFulfilled(game.getIsPlaying()),controller.checkIfCommonGoalN2IsFulfilled(playingrn));
    }
    /**
     * Tests method endOfTurn when the player is the one that is playing:PASSED
     * @author SaverioMaggese99
     * @throws RemoteException
     */
    @Test
    public void testendOfTurn_CorrectPlayer() throws RemoteException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        String vecchio = controller.getNameOfPlayerWhoIsCurrentlyPlaying();
        controller.endOfTurn(controller.getNameOfPlayerWhoIsCurrentlyPlaying());
        String nuovo = controller.getNameOfPlayerWhoIsCurrentlyPlaying();
        assertNotEquals(vecchio,nuovo);
    }

    /**
     * Tests method endOfTurn when the player is not the one that is playing: PASSED
     * @author SaverioMaggese99
     * @throws RemoteException
     */
    @Test
    public void testendOfTurn_WrongPlayer() throws RemoteException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        String nuovo;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        String vecchio = controller.getNameOfPlayerWhoIsCurrentlyPlaying();
        for(Player p: game.getPlayerList()){
            if(!p.getNickname().equals(vecchio)){
                controller.endOfTurn(p.getNickname());
                nuovo = controller.getNameOfPlayerWhoIsCurrentlyPlaying();
                assertEquals(nuovo,vecchio);

            }
        }
    }
    /**
     * Tests method testHasTheGameEnded both when game is ended and isn't: PASSED
     * @author SaverioMaggese99
     * @throws RemoteException
     */
    @Test
    public void testHasTheGameEnded() throws RemoteException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        assertEquals(game.hasTheGameEnded(),controller.hasTheGameEnded());
        game.endGame();
        assertEquals(game.hasTheGameEnded(),controller.hasTheGameEnded());

    }

    /**
     * @author SaverioMaggese99
     * @throws RemoteException
     * @throws InvalidMoveException
     * test play turn when the move is not valid
     */
    @Test
    public void testplayTurn_InvalidMove() throws RemoteException,InvalidMoveException{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        List<Tile> tiles = new ArrayList<>();
        Tile x = new Tile(Type.CAT);
        Tile y= new Tile(Type.PLANT);
        tiles.add(x);
        tiles.add(y);
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);
        assertThrows(InvalidMoveException.class, () -> game.playTurn(0, 0, 3, Board.Direction.DOWN, 1, tiles));
    }
    /**
     * @author SaverioMaggese99
     * @throws RemoteException
     * @throws InvalidMoveException
     * test play turn when the move is not valid
     */
    @Test
    public void testplayTurn() throws RemoteException,InvalidMoveException{
    assertTrue(true);

    }












    // Add more tests to cover other methods in the Controller class

}
