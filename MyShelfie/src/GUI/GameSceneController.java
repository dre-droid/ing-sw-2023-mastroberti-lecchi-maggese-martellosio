package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.java.it.polimi.ingsw.Model.*;

public class GameSceneController extends Application {

    @FXML
    public GridPane BoardGrid;
    private ClientNotificationRMIGUI clientRMI;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

    }

    public void setClient(ClientNotificationRMIGUI client){
        this.clientRMI = client;
        System.out.println(this.toString());
        System .out.println("AAAAAAAAAAAAAAAAAAAAAA");
        clientRMI.setGameSceneController(this);
    }

    public void updateBoard(TilePlacingSpot[][] grid){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Image image;
                ImageView imv;

                /*System.out.println("  0 1 2 3 4 5 6 7 8");
                for(int i = 0;i<9;i++) {
                    System.out.print(i+" ");
                    for (int j = 0; j < 9; j++) {
                        if (!grid[i][j].isAvailable()) System.out.print("x ");
                        else{
                            if(grid[i][j].isEmpty()) System.out.print("e ");
                            else{
                                Tile t = grid[i][j].showTileInThisPosition();
                                switch(t.getType()){
                                    case CAT: System.out.print("C ");break;
                                    case BOOK:System.out.print("B ");break;
                                    case GAME:System.out.print("G ");break;
                                    case FRAME:System.out.print("F ");break;
                                    case PLANT:System.out.print("P ");break;
                                    case TROPHY:System.out.print("T ");break;
                                }
                            }

                        }
                    }
                    System.out.println("");
                }*/

                for(int i=0;i<9;i++){
                    for(int j=0;j<9;j++){
                        if (grid[i][j].isAvailable()) {
                            if(!grid[i][j].isEmpty()) {
                                imv = new ImageView();
                                Tile t = grid[i][j].showTileInThisPosition();
                                switch(t.getType()){
                                    case CAT:{ image = new Image("item_tiles/Gatti1.1.png",45,45,true,true);
                                                System.out.print("C ");
                                    }break;
                                    case BOOK:{image = new Image("item_tiles/Libri1.1.png",45,45,true,true);
                                        System.out.print("B ");
                                    }break;
                                    case GAME:{image = new Image("item_tiles/Giochi1.1.png",45,45,true,true);
                                        System.out.print("G ");
                                    }break;
                                    case FRAME:{image = new Image("item_tiles/Cornici1.1.png",45,45,true,true);
                                        System.out.print("F ");
                                    }break;
                                    case PLANT:{image = new Image("item_tiles/Piante1.1.png",45,45,true,true);
                                        System.out.print("P ");
                                    }break;
                                    case TROPHY:{image = new Image("item_tiles/Trofei1.1.png",45,45,true,true);
                                        System.out.print("T ");
                                    }break;
                                    default:image = null;
                                }
                                imv.setImage(image);
                                BoardGrid.add(imv,j,i);
                                //imv.resize(imv.getFitHeight(), imv.getFitHeight());
                            }
                            else{
                                System.out.print("- ");
                            }
                        }else{
                            System.out.print("- ");
                        }
                    }
                    System.out.println();
                }
            }
        });

    }
}
