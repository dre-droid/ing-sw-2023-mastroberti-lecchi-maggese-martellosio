package main.java.it.polimi.ingsw.GUI;

import main.java.it.polimi.ingsw.Server.RMI.RMIinterface;
import main.java.it.polimi.ingsw.Server.Socket.ClientSocket;
import main.java.it.polimi.ingsw.Server.Socket.GUISocket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;

public class ConnectionTypeController  {

    @FXML
    private Text errorLabel;
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

    @FXML
    private TextField ipAddress;

    /**
     * This method is used to load the Loginscene after having selected a connection type between RMI and Socket.
     * At least one radiobutton has to be selected in order to proceed. Calls method checkIp to verify the correctness of the ip
     * inserted and set the TextLabel with an error message if there is one. Depending on the button selected creates
     * a client connected through Socket or RMI.
     * @param event event that triggers the method, i.e. the Next Button.
     */
    public void switchToLoginScene(ActionEvent event){
        try {
            if(MatchTypeGroup.getSelectedToggle()==null){
                errorLabel.setText("You must select the connection, RMI or Socket");
                return;
            }
            ToggleButton selectedToggle = (ToggleButton) MatchTypeGroup.getSelectedToggle();
            if (selectedToggle.isSelected()) {
                Scene scene;
                Parent root;
                ClassLoader classLoader = MainGUI.class.getClassLoader();
                URL fxmlPath = classLoader.getResource("LoginScene.fxml");
                if(fxmlPath==null){
                    throw new IllegalStateException("FXML non trovato");
                }
                FXMLLoader loader = new FXMLLoader(fxmlPath);
                //FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
                ClientSocket clientSocket = null;
                ClientNotificationRMIGUI clientRMI = null;

                String serverIp = ipAddress.getText();
                // check that ip is valid or empty: if the
                if (!Objects.equals(ipAddress.getText(), "")) {
                    if (!checkIp(serverIp)) {
                        errorLabel.setText("Wrong ip address, can't ping the server at this address");
                        return;
                    }
                }
                if (rButtonRMI.isSelected()) {
                    clientRMI = new ClientNotificationRMIGUI(serverIp);
                } else {
                    clientSocket = new GUISocket(serverIp);
                }

                root = loader.load();
                LoginSceneController loginSceneController = loader.getController();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                if (Objects.isNull(clientSocket)) {
                    //stage.setUserData(clientRMI);
                    loginSceneController.setClient(clientRMI);
                } else {
                    loginSceneController.setClient((GUISocket) clientSocket);
                }
                scene = new Scene(root);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to check if the server ip inserted by the players matches the actual id of the server
     * @param ip the ip of the server the player wants to connect to
     * @return true if the connection to the server was successful, false otherwise
     */

    private boolean checkIp(String ip){
        try{
            InetAddress inetAddress = InetAddress.getByName(ip);
            if(inetAddress instanceof Inet4Address) {
                if (ip.equals(inetAddress.getHostAddress())) {
                    if (ip.equals("127.0.0.2")) {
                        return false;
                    } else {
                        try {
                            Registry registryServer = LocateRegistry.getRegistry(ip);
                            RMIinterface serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");
                            serverRMI.ping();
                            return true;
                        } catch (RemoteException | NotBoundException e) {
                            return false;
                        }
                    }
                }
            }
            return false;
        }catch(UnknownHostException uhe){
            return false;
        }
    }


}
