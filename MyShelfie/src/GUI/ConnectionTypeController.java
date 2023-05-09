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


    public void switchToLoginScene(ActionEvent event){
        Scene scene;
        Parent root;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
        ClientSocket clientSocket = null;
        ClientRMI clientRMI = null;


        try {
            ToggleButton selectedToggle = (ToggleButton) MatchTypeGroup.getSelectedToggle();
            if (selectedToggle != null) {
                String selectedValue = (selectedToggle).getText();
                if (selectedValue.equals("RMI")) clientRMI = new ClientRMI();
                else clientSocket = new ClientSocket();
            }

            root = loader.load();
            LoginSceneController loginSceneController = loader.getController();
            if (Objects.isNull(clientSocket)) {
                loginSceneController.setClient(clientRMI);
            } else {
                loginSceneController.setClient(clientSocket);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
