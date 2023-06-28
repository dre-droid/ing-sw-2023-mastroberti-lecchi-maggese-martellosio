package main.java.it.polimi.ingsw.GUI;

import main.java.it.polimi.ingsw.Server.Socket.GUISocket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MatchTypeController {

    @FXML
    Image BoardID;

    @FXML
    public RadioButton rButton2p, rButton3p, rButton4p;

    private Stage stage;
    private Scene scene;
    private Parent root;

    private ClientNotificationRMIGUI clientRMI;
    private GUISocket clientSocket;


    /**
     * This method selects the number of players for the Game and switches to GameScene, if Game is already been created
     * switches to the LoginScene
     * @param event : selection of either 2 player button, 3 player button or 4 plater button
     */
    public void switchtoGameScene(ActionEvent event){
        if (rButton2p.isSelected() || rButton3p.isSelected() || rButton4p.isSelected()) {
            try {
                int numOfPlayers = -1;
                if (rButton2p.isSelected())
                    numOfPlayers = 2;
                if (rButton3p.isSelected())
                    numOfPlayers = 3;
                if (rButton4p.isSelected())
                    numOfPlayers = 4;

                // client RMI
                if (clientRMI != null) {
                    System.out.println("Rmi active in match type scene");
                    String nextScenePath;
                    if (clientRMI.createNewGame(numOfPlayers)) {
                        nextScenePath = "GameScene.fxml";
                    } else {
                        //alert about a game already being created
                        nextScenePath = "LoginScene.fxml";
                    }
                    ClassLoader classLoader = MainGUI.class.getClassLoader();
                    URL fxmlPath = classLoader.getResource(nextScenePath);
                    if(fxmlPath==null){
                        throw new IllegalStateException("FXML non trovato");
                    }
                    FXMLLoader loader = new FXMLLoader(fxmlPath);
                    Parent root = loader.load();
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


                    scene = new Scene(root);
                    stage.setScene(scene);

                    GameSceneController gsc = loader.getController();
                    gsc.setClient(clientRMI);
                    gsc.setPlayerName(clientRMI.getNickname());
                    stage.show();

                // client socket
                } else {
                    clientSocket.clientSpeaker(Integer.toString(numOfPlayers));
                    //TODO check that RMI hasnt created a game in the mean time
                    //e.g.
                    //[INFO]: Chosen nickname: dre
                    //[INFO]: Game is being created by another player...
                    //[REQUEST]: Choose the number of players for the game:
                    //2
                    //[INFO]: Somebody has already created a Game!
                    //[INFO]: Game is starting. der's turn.
                    //change scene to GameScene
                    socketSwitchToGameScene(event);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method is used to switch to the Game Scene using the JavaFX platform Thread
     * @param event the ActionEvent that triggers the game switch, i.e. the press of the Next Button.
     */
    private void socketSwitchToGameScene(ActionEvent event){
        Platform.runLater(() -> {
            ClassLoader classLoader = MainGUI.class.getClassLoader();
            URL fxmlPath = classLoader.getResource("GameScene.fxml");
            if(fxmlPath==null){
                throw new IllegalStateException("FXML non trovato");
            }
            FXMLLoader loader = new FXMLLoader(fxmlPath);
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            GameSceneController gameSceneController = loader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            gameSceneController.setClient(clientSocket);
            gameSceneController.runGameSceneThreads();
            new Thread(gameSceneController::socketMessageTextArea).start();
            gameSceneController.setPlayerName(clientSocket.getNickname());

            scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        });
    }

    public void setClient(ClientNotificationRMIGUI clientRMI){
        System.out.println("clientrmi not null in matchtypecontroller");
        this.clientRMI = clientRMI;
    }

    public void setClient(GUISocket clientSocket){
        this.clientSocket = clientSocket;
    }

}
