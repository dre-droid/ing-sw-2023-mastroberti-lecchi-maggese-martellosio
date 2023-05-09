package GUI;

import Server.ClientWithChoice;
import Server.RMI.ClientRMI;
import Server.Socket.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.awt.*;

import java.io.IOException;
import java.util.Objects;

public class LoginSceneController {
    @FXML
    public javafx.scene.control.Label InfoLabel;
    @FXML
    Label TopLabel;
    @FXML
    Button LoginButton;
    @FXML
    javafx.scene.control.TextField usernameText;
    @FXML
    AnchorPane LoginAnchorPane;



    private Stage stage;
    private Scene scene;
    private Parent root;
    private ClientSocket clientSocket;
    private ClientRMI clientRMI;

    /*
    public void login(ActionEvent event) throws IOException{
            String username = usernameText.getText();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Scene2.fxml"));
            root = loader.load();
    }*/

    public void switchToNextScene(ActionEvent event) {
        try {
            new Thread(() -> {
                while (true) {
                    if (clientSocket != null)
                        if (!Objects.isNull(clientSocket.stringGUI)) {
                            while (clientSocket.stringGUI.startsWith("[REQUEST]")) {
                                clientSocket.clientSpeaker(usernameText.getText());
                                InfoLabel.setText(clientSocket.stringGUI);
                            }
                            if (clientSocket.stringGUI.startsWith("[INFO]: Game is starting.")) break;
                        }

                }
            }).start();

            Parent root = FXMLLoader.load(getClass().getResource("MatchType.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void setClient(ClientSocket client){
        this.clientSocket = client;
        client.runServer();
    }

    public void setClient(ClientRMI client){
        this.clientRMI = client;
    }
}
