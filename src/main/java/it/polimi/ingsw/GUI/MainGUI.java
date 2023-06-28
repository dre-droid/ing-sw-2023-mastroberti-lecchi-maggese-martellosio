package main.java.it.polimi.ingsw.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;


public class MainGUI extends Application {



    public static void main(String[] args) {

    }

    /**
     * This method starts the GUI loading ConnectionType scene: Entry point of JavaFX application
     * @param stage the primary stage of the application
     * @throws Exception if there are errors in the execution i.e. the fxml path is null
     */

    @Override
    public void start(Stage stage) throws Exception {

        try{
        //Font.loadFont(getClass().getResource("JokermanRegular.ttf").toExternalForm(), 10);
            ClassLoader classLoader = MainGUI.class.getClassLoader();
            URL fxmlPath = classLoader.getResource("ConnectionType.fxml");
            if(fxmlPath==null){
                throw new IllegalStateException("FXML non trovato");
            }
            FXMLLoader loader = new FXMLLoader(fxmlPath);
        Parent root = loader.load();
        Scene scene1 = new Scene(root);
        stage.setScene(scene1);
        stage.setResizable(false);
        stage.setTitle("MyShelfie");
        stage.getIcons().add(new Image("Publisher_material/Icon.png"));

        stage.show();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
