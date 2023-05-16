package GUI;

import Server.ClientWithChoice;
import Server.RMI.ClientNotificationRMI;
import Server.RMI.ClientRMI;
import Server.Socket.ClientSocket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import main.java.it.polimi.ingsw.Model.Game;

import javax.swing.*;
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
    private FXMLLoader loader;
    private ClientSocket clientSocket;
    private ClientNotificationRMIGUI clientRMI;
    private boolean repeat = false;

    /**
     * Method is called when button is pressed in ConnectionType scene
     */
    public void switchToNextScene(ActionEvent event) {
        try {
            if (Objects.isNull(clientSocket)) handleRMI(event);
            else new Thread(() -> handleSocket(event)).start();
            //TODO what if someone spams the button?
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void handleRMI(ActionEvent event) throws IOException{
        if(clientRMI!=null){
            System.out.println("rmi active on login scene");
            clientRMI.setnickname(usernameText.getText());
            try{
                FXMLLoader loader;
                String errorMessage="";
                clientRMI.startNotificationServer();
                int outcome = clientRMI.joinGame();
                System.out.println(outcome);
                String nextScenePath="";
                switch(outcome){
                    case -1:{
                        nextScenePath = "MatchType.fxml";
                        System.out.println("createnewgamecommand");
                        loader = new FXMLLoader(getClass().getResource(nextScenePath));
                        Parent root = loader.load();
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        scene = new Scene(root);
                        stage.setScene(scene);
                        MatchTypeController mtt = loader.getController();
                        mtt.setClient(clientRMI);
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
                        nextScenePath = "GameScene.fxml";
                        loader = new FXMLLoader(getClass().getResource(nextScenePath));
                        Parent root = loader.load();
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        scene = new Scene(root);
                        stage.setScene(scene);
                        GameSceneController gsc = loader.getController();
                        gsc.setClient(clientRMI);
                        gsc.getClientRMI().updateGUIAtBeginningOfGame();

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
        Parent root = FXMLLoader.load(getClass().getResource("MatchType.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void handleSocket(ActionEvent event){
        try {
            clientSocket.clientSpeaker(usernameText.getText());     //send username
            //wait for server response being handled by clientSocket
            synchronized (clientSocket) {
                while (!clientSocket.nextScene.equals("Unchanged") && !clientSocket.nextScene.equals("MatchType") && !clientSocket.nextScene.equals("GameScene")) clientSocket.wait();
            }
            //TODO why does it get stuck in wait() (i think) after inserting wrong nickname? (have to press twice but it sort of works)
            if (clientSocket.nextScene.equals("MatchType")) {
                //change scene to MatchType (Platform.runLater() needed to update UI when not in main Thread)
                Platform.runLater(() -> {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("MatchType.fxml"));
                    try {
                        root = loader.load();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    MatchTypeController matchTypeController = loader.getController();
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    matchTypeController.setClient(clientSocket);

                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                });

            }
            if (clientSocket.nextScene.equals("GameScene")) {
                //change scene to GameScene
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

                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                });
            }
            if (clientSocket.nextScene.equals("Unchanged")){
                Platform.runLater(() -> {
                    InfoLabel.setText("Invalid nickname. Try again!");
                });
            }
        }catch(InterruptedException | RuntimeException e){
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
