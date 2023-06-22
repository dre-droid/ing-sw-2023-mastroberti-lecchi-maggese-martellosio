package GUI;

import Server.ClientWithChoice;
import Server.RMI.ClientRMI;
import Server.RMI.RMIinterface;
import Server.Socket.ClientSocket;
import Server.Socket.GUISocket;
import javafx.application.Application;
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginScene.fxml"));
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
