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
    public void testJoinGame() {
        String nickname = "Player2";
        int result = controller.joinGame(nickname);
        assertEquals(-1,result);
        assertNotEquals(-4,result);
    }

    @Test
    public void testJoinGame_GameAlreadyStarted() {
        String nickname = "Player2";
        assertEquals(0, game.getNumOfPlayers());
    }

    @Test
    public void testJoinGame_NoGameToJoin() {
        game = null;
        String nickname = "Player2";

    }

    @Test
    public void testRemovePlayer() throws RemoteException{
        String nickname = "Saverio";
        String other = "Other";
        controller.createNewGame(nickname,2);
        game.addPlayer(other);
        assertEquals(2, controller.getGame().getLeaderBoard().size());

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
     * Test the method hasGameBeenCreated
     * @throws RemoteException
     */
    @Test
    public void testHasGamebeenCreated() throws RemoteException{
        String nickname = "Save";
        int num = 4;
        controller.createNewGame(nickname,num);
        boolean result = controller.hasGameBeenCreated();
        assertTrue(result);

    }




    // Add more tests to cover other methods in the Controller class

}
