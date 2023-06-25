package GUI;

import Server.Socket.ClientSocket;
import Server.Socket.GUISocket;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginSceneController{
    @FXML
    public Label messageTextArea;
    @FXML
    javafx.scene.control.TextField usernameText;
    @FXML
    AnchorPane LoginAnchorPane;




    private Stage stage;
    private Scene scene;
    private Parent root;
    private FXMLLoader loader;
    private GUISocket clientSocket;
    private ClientNotificationRMIGUI clientRMI;
    private boolean alive = false;

    private boolean disconnected;

    public void setClient(GUISocket client){
        this.clientSocket = client;
        if(disconnected){
            messageTextArea.setText("You got disconnected...");
        }
        client.runServer();
    }

    public void setDisconnected(boolean disconnected){
        this.disconnected = disconnected;
    }

    public void setClient(ClientNotificationRMIGUI client){

        this.clientRMI = client;
        if(disconnected){
            messageTextArea.setText("You got disconnected...");
        }
    }

    /**
     * Method is called when button is pressed in ConnectionType scene
     */
    public void switchToNextScene(ActionEvent event) {
        if (!alive) { //handles spamming button
            if (Objects.isNull(clientSocket))
                new Thread(() -> handleRMIv2(event)).start();
            else {
                new Thread(() -> handleSocket(event)).start();
            }
        }
        else
            updateLabelText(messageTextArea, messageTextArea.getText());
    }


    private void handleRMIv2(ActionEvent event){
        alive = true;
        if(clientRMI!=null){
            System.out.println("rmi active on login scene");
            clientRMI.setnickname(usernameText.getText());
            String nextScenePath;
            try{
                FXMLLoader loader;
                String errorMessage="";
                clientRMI.startNotificationServer();
                if(clientRMI.hasGameStarted()){
                    System.out.println("riconnessione in corso...");
                    if(clientRMI.reconnectToGame()){
                        //switch to game scene
                        nextScenePath = "GameScene.fxml";
                        loader = new FXMLLoader(getClass().getResource(nextScenePath));
                        Parent root = loader.load();
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        scene = new Scene(root);
                        stage.setScene(scene);

                        GameSceneController gsc = loader.getController();
                        gsc.setClient(clientRMI);
                        if(gsc.getClientRMI().hasGameStarted())
                            gsc.getClientRMI().updateGUIAtBeginningOfGame();
                        gsc.setPlayerName(clientRMI.getNickname());

                        stage.setResizable(false);
                        stage.show();

                    }
                    else{
                        updateLabelText(messageTextArea, "Non è stato possibile riconnettersi, prova con un altro nickname");
                        System.out.println("ERRORE RICONNESIONE");
                        return;

                    }
                }
                else{
                    System.out.println("game has not started");
                }
                int outcome = clientRMI.joinLobby();
                System.out.println(outcome);
                switch(outcome){
                    case -1:{
                        errorMessage = "Nickname already in use";
                        loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
                        updateLabelText(messageTextArea, errorMessage);
                    }break;
                    case 0:{
                       clientRMI.periodicPing();
                        if(clientRMI.isGameBeingCreated() && !clientRMI.firstInLobby(clientRMI.getNickname())){
                            System.out.println("Game is being created by another player...");
                            updateLabelText(messageTextArea, "Game is being created by another player...");
                        }
                        synchronized (clientRMI){
                            while(clientRMI.joinGameOutcome == -5){
                                clientRMI.wait();
                            }
                        }
                        switch(clientRMI.joinGameOutcome){
                            case -1:{
                                nextScenePath = "MatchType.fxml";
                                System.out.println("createnewgamecommand");
                                loader = new FXMLLoader(getClass().getResource(nextScenePath));
                                Parent root = loader.load();
                                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                scene = new Scene(root);
                                Platform.runLater(() -> stage.setScene(scene));
                                stage.setOnCloseRequest(e->{
                                    Platform.exit();
                                    System.exit(0);
                                });
                                MatchTypeController mtt = loader.getController();
                                mtt.setClient(clientRMI);
                                Platform.runLater(stage::show);
                            }break;
                            case -2:{
                                errorMessage = "The game has already started";
                                loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
                                updateLabelText(messageTextArea, errorMessage);
                            }break;
                            case 0:{
                                nextScenePath = "GameScene.fxml";
                                loader = new FXMLLoader(getClass().getResource(nextScenePath));
                                Parent root = loader.load();
                                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                scene = new Scene(root);
                                Platform.runLater(() -> stage.setScene(scene));

                                GameSceneController gsc = loader.getController();
                                gsc.setClient(clientRMI);
                                if(gsc.getClientRMI().hasGameStarted())
                                    gsc.getClientRMI().updateGUIAtBeginningOfGame();
                                gsc.setPlayerName(clientRMI.getNickname());

                                stage.setResizable(false);
                                Platform.runLater(stage::show);
                            }break;
                            default:{
                                //not yet implemented
                                loader = new FXMLLoader(getClass().getResource("error.fxml"));
                            }
                        }
                    }break;
                }
                return;
            }catch(RemoteException e){
                //send to error page
                updateLabelText(messageTextArea, "cannot connect to server");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                alive = false;
            }
        }
       /* Parent root = FXMLLoader.load(getClass().getResource("MatchType.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();*/
    }

    private void handleSocket(ActionEvent event){
        try {
            alive = true;
            clientSocket.clientSpeaker(usernameText.getText());
            // wait for server response being handled by clientSocket
            do {
                synchronized (clientSocket.nextSceneLock) {
                    while (clientSocket.nextScene.equals("")) clientSocket.nextSceneLock.wait();
                }

                if (clientSocket.nextScene.equals("MatchType")) {
                    //change scene to MatchType (Platform.runLater() needed to update UI when not in main Thread)
                    socketSwitchToMatchTypeScene(event);
                } else if (clientSocket.nextScene.equals("GameScene")) {
                    //change scene to GameScene
                    socketSwitchToGameScene(event);
                } else {
                    // display error message from server
                    String string = clientSocket.nextScene;
                    Platform.runLater(() -> updateLabelText(messageTextArea, string));
                    clientSocket.nextScene = "";
                    if(string.startsWith("Nickname already in use") || string.startsWith("Invalid nickname") || string.startsWith("The game already started"))
                        break;
                    if (string.startsWith("The game already started, you can't join, try again later")){
                        System.exit(0);
                    }
                }
            }while(!clientSocket.nextScene.equals("GameScene") && !clientSocket.nextScene.equals("MatchType"));
        }catch(InterruptedException | RuntimeException e){
            e.printStackTrace();
        }
        finally {
            //set thread to dead, and reset nextScene variable
            alive = false;
            clientSocket.nextScene = "";
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
    private void socketSwitchToMatchTypeScene(ActionEvent event){
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
            stage.setResizable(false);
            stage.show();
        });
    }

    private void updateLabelText(Label label, String string) {
        Platform.runLater(() -> {
            // if message is unchanged, animate a yellow blink to give feedback
            if (messageTextArea.getText().equals(string)) {
                Duration ANIMATION_DURATION = Duration.seconds(0.15);

                Timeline timeline = new Timeline();

                KeyValue keyValue1 = new KeyValue(label.textFillProperty(), Color.YELLOW);
                KeyFrame keyFrame1 = new KeyFrame(ANIMATION_DURATION, keyValue1);
                KeyValue keyValue2 = new KeyValue(label.textFillProperty(), Color.WHITE);
                KeyFrame keyFrame2 = new KeyFrame(ANIMATION_DURATION.multiply(2), keyValue2);

                timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);

                timeline.setOnFinished(event -> label.setText(string));

                timeline.play(); // start the timeline
            } else
                messageTextArea.setText(string);
        });
    }



}