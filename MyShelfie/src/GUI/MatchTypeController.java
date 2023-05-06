package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class MatchTypeController {

    @FXML
    Image BoardID;



    private Stage stage;
    private Scene scene;
    private Parent root;
    public void switchtoGameScene(ActionEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("GameScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
}
