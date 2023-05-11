package GUI;

import Server.ClientWithChoice;
import Server.RMI.ClientNotificationRMI;
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
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;
import java.util.Random;

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
    private ClientNotificationRMIGUI clientRMI;

    /*
    public void login(ActionEvent event) throws IOException{
            String username = usernameText.getText();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Scene2.fxml"));
            root = loader.load();
    }*/

    public void switchToNextScene(ActionEvent event) {
        try {
            //connection to rmi server
            if(clientRMI!=null){
                System.out.println("rmi active on login scene");
                clientRMI.setnickname(usernameText.getText());
                try{
                    FXMLLoader loader;
                    String errorMessage="";
                    clientRMI.startNotificationServer();
                    int outcome = clientRMI.joinGame();
                    System.out.println(outcome);
                    String nextScenPath="";
                    switch(outcome){
                        case -1:{
                            nextScenPath = "MatchType.fxml";
                            System.out.println("createnewgamecommand");
                            loader = new FXMLLoader(getClass().getResource(nextScenPath));
                            Parent root = loader.load();
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            scene = new Scene(root);
                            stage.setScene(scene);
                            MatchTypeController mtt = loader.getController();
                            mtt.setClientRMI(clientRMI);
                            stage.show();
                        }break;
                        case -2:{
                            errorMessage = "The game has already started";
                            loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
                        }break;
                        case -3:{
                            //error: need to change nickname -> show alert and do nothing
                            errorMessage = "Nickname already in use";
                            loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
                        }break;
                        case 0:{
                            nextScenPath = "GameScene.fxml";
                            loader = new FXMLLoader(getClass().getResource(nextScenPath));
                            Parent root = loader.load();
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            scene = new Scene(root);
                            stage.setScene(scene);
                            LoginSceneController lsc = loader.getController();
                            lsc.setClient(clientRMI);
                            stage.show();
                        }break;
                        default:{
                            //not yet implemented
                            loader = new FXMLLoader(getClass().getResource("error.fxml"));
                        }
                    }
                    return;
                }catch(RemoteException e){
                    //send to error page
                    e.printStackTrace();
                }
            }

            new Thread(() -> {
                while (true) {
                    if (clientSocket != null)
                        if (!Objects.isNull(clientSocket.messageFromServer)) {
                            while (clientSocket.messageFromServer.startsWith("[REQUEST]")) {
                                clientSocket.clientSpeaker(usernameText.getText());
                                InfoLabel.setText(clientSocket.messageFromServer);
                            }
                            if (clientSocket.messageFromServer.startsWith("[INFO]: Game is starting.")) break;
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

    public void setClient(ClientNotificationRMIGUI client){
        this.clientRMI = client;
    }


}
