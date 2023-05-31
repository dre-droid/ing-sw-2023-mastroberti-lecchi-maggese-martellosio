package GUI;

import Server.ClientWithChoice;
import Server.RMI.ClientRMI;
import Server.Socket.ClientSocket;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class ConnectionTypeController  {
    @FXML
    ToggleGroup MatchTypeGroup;
    @FXML
    Label ConnectionLabel;
    @FXML
    Button CreateCreateGameButton;

    @FXML
    private RadioButton ButtonSocket;
    @FXML
    private RadioButton rButtonRMI;


    public void switchToLoginScene(ActionEvent event){
        ToggleButton selectedToggle = (ToggleButton) MatchTypeGroup.getSelectedToggle();
        if (selectedToggle.isSelected()) {
            Scene scene;
            Parent root;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
            ClientSocket clientSocket = null;
            ClientNotificationRMIGUI clientRMI = null;

            try {
                if (rButtonRMI.isSelected()) clientRMI = new ClientNotificationRMIGUI();
                else clientSocket = new ClientSocket(true);

                root = loader.load();
                LoginSceneController loginSceneController = loader.getController();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                if (Objects.isNull(clientSocket)) {
                    //stage.setUserData(clientRMI);
                    loginSceneController.setClient(clientRMI);
                } else {
                    loginSceneController.setClient(clientSocket);
                }
                scene = new Scene(root);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
