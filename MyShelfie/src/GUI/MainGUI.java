package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.Image;


public class MainGUI extends Application {



    public static void main(String[] args) {

    }

    @Override
    public void start(Stage stage) throws Exception {

        try{
        Font.loadFont(getClass().getResource("JokermanRegular.ttf").toExternalForm(), 10);
        Parent root = FXMLLoader.load(getClass().getResource("ConnectionType.fxml"));
        Scene scene1 = new Scene(root);
        stage.setScene(scene1);
        stage.setResizable(false);
        stage.setTitle("MyShelfie");

        stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
