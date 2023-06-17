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
    private Board board;
    private ArrayList<Player> p;


    @BeforeEach
    public void setUp() throws RemoteException {
        server = new Server();
        serverRMI = new ServerRMI(controller,server);
        serverSock = new ServerSock(controller,server);
        controller = new Controller(server);
        controller.setServerSock(serverSock);
        game = controller.getGame();

    }

    @Test
    public void testCreateNewGame() throws RemoteException {
        game = null;
        String nickname = "Saverio";
        int numOfPlayers = 4;

        boolean result = controller.createNewGame(nickname, numOfPlayers);

        assertTrue(result);
        assertTrue(game.hasGameStarted());
        assertEquals(1, game.getNumOfPlayers());
    }

    @Test
    public void testCreateNewGame_GameAlreadyRunning() throws RemoteException {
        String nickname = "Saverio";
        int numOfPlayers = 4;


        boolean result = controller.createNewGame(nickname, numOfPlayers);

        assertFalse(result);
        assertEquals(0, game.getNumOfPlayers());
    }

    @Test
    public void testJoinGame() {
        String nickname = "Player2";

        int result = controller.joinGame(nickname);


        assertEquals(1, game.getNumOfPlayers());
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
    public void testRemovePlayer() {
        String nickname = "Player3";

        game.addPlayer(nickname);
        controller.removePlayer(nickname);

        assertEquals(0, game.getNumOfPlayers());
    }

    @Test
    public void testHasGameStarted() {
        boolean result = controller.hasGameStarted();
        assertTrue(result);
    }




    // Add more tests to cover other methods in the Controller class

}
