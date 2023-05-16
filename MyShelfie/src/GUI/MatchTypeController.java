package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

import java.awt.*;
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

    public void switchtoGameScene(ActionEvent event){
        try {
            if(clientRMI!=null){
                System.out.println("Rmi active in match type scene");
                String nextScenePath;
                int numOfPlayers=-1;
                if(rButton2p.isSelected())
                    numOfPlayers = 2;
                if(rButton3p.isSelected())
                    numOfPlayers = 3;
                if(rButton4p.isSelected())
                    numOfPlayers = 4;
                if(clientRMI.createNewGame(numOfPlayers)){
                    nextScenePath = "GameScene.fxml";
                }else{
                    //alert about a game already being created
                    nextScenePath = "LoginScene.fxml";
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource(nextScenePath));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                GameSceneController gsc = loader.getController();
                gsc.setClient(clientRMI);
                stage.show();
                return;
            }

            Parent root = FXMLLoader.load(getClass().getResource("GameScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setClientRMI(ClientNotificationRMIGUI clientRMI){
        System.out.println("clientrmi not null in matchtypecontroller");
        this.clientRMI = clientRMI;
    }

}
