package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;


public class MainGUI extends Application {



    public static void main(String[] args) {

    }

    @Override
    public void start(Stage stage) throws Exception {

        try{
        Parent root = FXMLLoader.load(getClass().getResource("LoginScene.fxml"));
        Scene scene1 = new Scene(root);
        stage.setScene(scene1);
        //Image icon = new Image("Icon.png");
        //stage.getIcons().add(icon);
        stage.setTitle("MyShelfie");

        stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
