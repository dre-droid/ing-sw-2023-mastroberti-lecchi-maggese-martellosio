package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.awt.*;

import java.io.IOException;

public class LoginSceneController {

    @FXML
    Label TopLabel;
    @FXML
    Button LoginButton;
    @FXML
    javafx.scene.control.TextField usernameText;


    private Stage stage;
    private Scene scene;
    private Parent root;

    public void login(ActionEvent event) throws IOException{
            String username = usernameText.getText();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Scene2.fxml"));
            root = loader.load();
    }

    public void switchtoConnectionTypeScene(ActionEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("ConnectionType.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();



    }
}
