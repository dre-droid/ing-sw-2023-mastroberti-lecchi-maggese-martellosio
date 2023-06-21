package GUI;

import Server.Socket.ClientSocket;
import Server.Socket.GUISocket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.geometry.Insets;
import java.io.IOException;

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
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(nextScenePath));
                    Parent root = loader.load();
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e->{
                        e.consume();
                        Popup popup = new Popup();
                        HBox buttons = new HBox(30);
                        javafx.scene.control.Button quit = new javafx.scene.control.Button("Quit game");
                        javafx.scene.control.Button cancel = new Button("Cancel");
                        buttons.getChildren().addAll(quit, cancel);
                        buttons.setStyle("-fx-background-color: white; -fx-padding: 13px;");
                        buttons.setPadding(new Insets(5,5,5,5));
                        popup.getContent().add(buttons);
                        popup.show(stage);
                        quit.setOnAction(eq->{
                            if(clientRMI!=null){
                                clientRMI.quitGame();
                            }
                            Platform.exit();
                        });
                        cancel.setOnAction(ec->{
                            popup.hide();
                        });
                    });
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

    private void socketSwitchToGameScene(ActionEvent event){
        Platform.runLater(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameScene.fxml"));
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
