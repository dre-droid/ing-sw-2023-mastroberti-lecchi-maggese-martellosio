package main.java.it.polimi.ingsw.GUI;

import main.java.it.polimi.ingsw.Server.Socket.GUISocket;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Objects;

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
                        ClassLoader classLoader = MainGUI.class.getClassLoader();
                        URL fxmlPath = classLoader.getResource("GameScene.fxml");
                        if(fxmlPath==null){
                            throw new IllegalStateException("FXML non trovato");
                        }
                        loader = new FXMLLoader(fxmlPath);
                        Parent root = loader.load();
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        scene = new Scene(root);
                        Platform.runLater(()->stage.setScene(scene));

                        GameSceneController gsc = loader.getController();
                        gsc.setClient(clientRMI);
                        if(gsc.getClientRMI().hasGameStarted())
                            gsc.getClientRMI().updateGUIAtBeginningOfGame();
                        gsc.setPlayerName(clientRMI.getNickname());

                        Platform.runLater(()->stage.setResizable(false));
                        Platform.runLater(stage::show);

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
                        loader = new FXMLLoader(getClass().getResource("../../../../../resources/LoginScene.fxml"));
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
                                System.out.println("createnewgamecommand");
                                ClassLoader classLoader = MainGUI.class.getClassLoader();
                                URL fxmlPath = classLoader.getResource("MatchType.fxml");
                                if(fxmlPath==null){
                                    throw new IllegalStateException("FXML non trovato");
                                }
                                loader = new FXMLLoader(fxmlPath);
                                Parent root = loader.load();
                                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                scene = new Scene(root);
                                Platform.runLater(() -> stage.setScene(scene));
                                Platform.runLater(()->stage.setOnCloseRequest(e->{
                                    Platform.exit();
                                    System.exit(0);
                                }));
                                MatchTypeController mtt = loader.getController();
                                mtt.setClient(clientRMI);

                                Platform.runLater(stage::show);
                            }break;
                            case -2:{
                                errorMessage = "The game has already started";
                                //loader = new FXMLLoader(getClass().getResource("../../../../../resources/fxmls/LoginScene.fxml"));
                                updateLabelText(messageTextArea, errorMessage);
                            }break;
                            case 0:{
                                ClassLoader classLoader = MainGUI.class.getClassLoader();
                                URL fxmlPath = classLoader.getResource("GameScene.fxml");
                                if(fxmlPath==null){
                                    throw new IllegalStateException("FXML non trovato");
                                }
                                loader = new FXMLLoader(fxmlPath);
                                Parent root = loader.load();
                                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                scene = new Scene(root);
                                Platform.runLater(() -> stage.setScene(scene));

                                GameSceneController gsc = loader.getController();
                                gsc.setClient(clientRMI);
                                if(gsc.getClientRMI().hasGameStarted())
                                    gsc.getClientRMI().updateGUIAtBeginningOfGame();
                                gsc.setPlayerName(clientRMI.getNickname());

                                Platform.runLater(()->stage.setResizable(false));
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
    }

    private void handleSocket(ActionEvent event){
        try {
            alive = true;
            clientSocket.clientSpeaker(usernameText.getText());
            // wait for server response being handled by clientSocket
            System.out.println("print1");
            String string;
            boolean flag = true;
            do {
                synchronized (clientSocket.nextSceneLock) {
                    System.out.println("print2");
                    while (clientSocket.nextScene.equals("")) clientSocket.nextSceneLock.wait();
                    string = clientSocket.nextScene;
                    clientSocket.nextScene = "";
                    System.out.println("print3 " + clientSocket.nextScene);
                }
                System.out.println("print4");
                if (string.equals("MatchType")) {
                    //change scene to MatchType (Platform.runLater() needed to update UI when not in main Thread)
                    socketSwitchToMatchTypeScene(event);
                    flag = false;
                    System.out.println("print5");
                } else if (string.equals("GameScene")) {
                    //change scene to GameScene
                    socketSwitchToGameScene(event);
                    flag = false;
                    System.out.println("print6");
                } else {
                    updateLabelText(messageTextArea, string);
                    System.out.println("print7");
                    if(string.startsWith("Nickname already in use") || string.startsWith("Invalid nickname"))
                        break;
                    if (string.startsWith("The game already started, you can't join, try again later")){
                        //String ip = this.clientSocket.getIp();
                        //this.clientSocket = new GUISocket(ip);
                        System.exit(0);
                    }
                }
            }while(flag);
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
            ClassLoader classLoader = MainGUI.class.getClassLoader();
            URL fxmlPath = classLoader.getResource("GameScene.fxml");
            if(fxmlPath==null){
                throw new IllegalStateException("FXML non trovato");
            }
            loader = new FXMLLoader(fxmlPath);
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                System.out.println("error in loading the next scene");
            }

            GameSceneController gameSceneController = loader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            gameSceneController.setClient(clientSocket);
            gameSceneController.runGameSceneThreads();
            new Thread(gameSceneController::socketMessageTextArea).start();
            gameSceneController.setPlayerName(clientSocket.getNickname());

            scene = new Scene(root);
            Platform.runLater(()->stage.setScene(scene));
            Platform.runLater(()->stage.setResizable(false));
            Platform.runLater(()->stage.show());
        });
    }
    private void socketSwitchToMatchTypeScene(ActionEvent event){
        Platform.runLater(() -> {
            ClassLoader classLoader = MainGUI.class.getClassLoader();
            URL fxmlPath = classLoader.getResource("MatchType.fxml");
            if(fxmlPath==null){
                throw new IllegalStateException("FXML non trovato");
            }
            loader = new FXMLLoader(fxmlPath);
            Parent root = null;
            try {
                root = loader.load();
            } catch (IOException e) {
                System.out.println("error in loading the next scene");
            }

            MatchTypeController matchTypeController = loader.getController();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            matchTypeController.setClient(clientSocket);
            scene = new Scene(root);
            Platform.runLater(()->{
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
                stage.setOnCloseRequest(e->{
                    Platform.exit();
                    System.exit(0);
                });
            });


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