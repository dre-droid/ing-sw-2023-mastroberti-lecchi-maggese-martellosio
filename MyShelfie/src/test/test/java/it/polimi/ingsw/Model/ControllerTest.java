package test.test.java.it.polimi.ingsw.Model;

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
    public void test_getNameOfPlayerWhoIsCurrentlyPlaying() throws RemoteException{
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
    public void test_isMyTurn() throws RemoteException{
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
    public void test_drawFromBoard() throws RemoteException,InvalidMoveException,Exception{
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
     * Test the method drawFromBoard:
     * @throws RemoteException,Exception,InvalidMoveException
     * @author SaverioMaggese99
     */
    @Test
    public void test_insertTilesInShelf() throws Exception{
        String nick1 = "Save";
        String nick2 = "Chiara";
        int num = 2 ;
        controller.createNewGame(nick1,num);
        game = controller.getGame();
        controller.joinGame(nick2);


    }










    // Add more tests to cover other methods in the Controller class

}
