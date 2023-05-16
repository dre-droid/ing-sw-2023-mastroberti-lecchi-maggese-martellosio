package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSceneController extends Application {

    private boolean leaderboardCheck = false;
    @FXML
    private TableView TableLeaderboard;

    @FXML
    private TableColumn Player;

    @FXML private TableColumn Score;

    @FXML
    private GridPane BoardGrid;

    @FXML
    private ImageView MyPGC;

    @FXML
    private ImageView CG1;

    @FXML
    private ImageView CG2;
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
        //System .out.println("AAAAAAAAAAAAAAAAAAAAAA");
        clientRMI.setGameSceneController(this);
    }

    public void setPersonalGoalCardImage(PersonalGoalCard pgc, Map<Integer, PersonalGoalCard> pgcMap){
        int id=-1;
        Image image=null;
        boolean flag;
        for(Map.Entry<Integer, PersonalGoalCard> pgcKey: pgcMap.entrySet()){
            /*System.out.println(pgcKey.getValue().toString());
            System.out.println("/////////////////////////////");
            System.out.println(pgc.toString());*/
            Tile[][] tps = pgcKey.getValue().getValidTiles().getGridForDisplay();
            Tile[][] tpsPlayer = pgc.getValidTiles().getGridForDisplay();
            flag =true;
            for(int i=0;i<6;i++){
                for(int j=0;j<5;j++){
                    if(tps[i][j]!=null && tpsPlayer[i][j]!=null){
                        if(tps[i][j].getType() != tpsPlayer[i][j].getType())
                            flag = false;
                    }else{
                        if(!(tps[i][j]==null && tpsPlayer[i][j]==null))
                            flag = false;
                    }
                }
            }
            if(flag){
                id= pgcKey.getKey();
                System.out.println("---"+id);
            }
            flag = true;

            //System.out.println("-------------------------------");
        }
        switch (id){
            case 1:image = new Image("personal_goal_cards/Personal_Goals.png") ;break;
            case 2:image = new Image("personal_goal_cards/Personal_Goals2.png") ;break;
            case 3:image = new Image("personal_goal_cards/Personal_Goals3.png") ;break;
            case 4:image = new Image("personal_goal_cards/Personal_Goals4.png") ;break;
            case 5:image = new Image("personal_goal_cards/Personal_Goals5.png") ;break;
            case 6:image = new Image("personal_goal_cards/Personal_Goals6.png") ;break;
            case 7:image = new Image("personal_goal_cards/Personal_Goals7.png") ;break;
            case 8:image = new Image("personal_goal_cards/Personal_Goals8.png") ;break;
            case 9:image = new Image("personal_goal_cards/Personal_Goals9.png") ;break;
            case 10:image = new Image("personal_goal_cards/Personal_Goals10.png") ;break;
            case 11:image = new Image("personal_goal_cards/Personal_Goals11.png") ;break;
            case 12:image = new Image("personal_goal_cards/Personal_Goals12.png") ;break;
            default:System.out.println("MANNAGGIAAA");
        }
        MyPGC.setImage(image);
    }

    public void updateBoard(TilePlacingSpot[][] grid){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Image image;
                ImageView imv;
                for(int i=0;i<9;i++){
                    for(int j=0;j<9;j++){
                        if (grid[i][j].isAvailable()) {
                            if(!grid[i][j].isEmpty()) {
                                imv = new ImageView();
                                Tile t = grid[i][j].showTileInThisPosition();
                                switch(t.getType()){
                                    case CAT:{ image = new Image("item_tiles/Gatti1.1.png",45,45,true,true);
                                                //System.out.print("C ");
                                    }break;
                                    case BOOK:{image = new Image("item_tiles/Libri1.1.png",45,45,true,true);
                                        //System.out.print("B ");
                                    }break;
                                    case GAME:{image = new Image("item_tiles/Giochi1.1.png",45,45,true,true);
                                        //System.out.print("G ");
                                    }break;
                                    case FRAME:{image = new Image("item_tiles/Cornici1.1.png",45,45,true,true);
                                        //System.out.print("F ");
                                    }break;
                                    case PLANT:{image = new Image("item_tiles/Piante1.1.png",45,45,true,true);
                                        //System.out.print("P ");
                                    }break;
                                    case TROPHY:{image = new Image("item_tiles/Trofei1.1.png",45,45,true,true);
                                        //System.out.print("T ");
                                    }break;
                                    default:image = null;
                                }
                                imv.setImage(image);
                                BoardGrid.add(imv,j,i);
                                //imv.resize(imv.getFitHeight(), imv.getFitHeight());
                            }
                            else{
                                //System.out.print("- ");
                            }
                        }else{
                            //System.out.print("- ");
                        }
                    }
                    //System.out.println();
                }
            }
        });

    }

    public ClientNotificationRMIGUI getClientRMI(){
        return this.clientRMI;
    }

    public void createLeaderboard(List<Player> leaderboard){
        if(!leaderboardCheck){
            leaderboardCheck=true;
            TableColumn player = new TableColumn("Player");
            TableColumn score = new TableColumn("Score");
            TableLeaderboard.getColumns().addAll(player, score);
            TableLeaderboard.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            final ObservableList<TableRecord> data = FXCollections.observableArrayList();
            for(Player p: leaderboard){
                data.add(new TableRecord(p.getNickname(), ""+p.getScore()));
            }

            player.setCellValueFactory(new PropertyValueFactory<TableRecord,String>("player"));
            score.setCellValueFactory(new PropertyValueFactory<TableRecord, String>("score"));

            TableLeaderboard.setItems(data);
        }

    }

    public void setCommonGoalCardImage(CommonGoalCard cgc,int n){
        int id = cgc.getStrategyID();
        Image image = null;
        switch(id){
            case 1:image = new Image("common_goal_cards/11.jpg");break;
            case 2:image = new Image("common_goal_cards/9.jpg");break;
            case 3:image = new Image("common_goal_cards/8.jpg");break;
            case 4:image = new Image("common_goal_cards/3.jpg");break;
            case 5:image = new Image("common_goal_cards/7.jpg");break;
            case 6:image = new Image("common_goal_cards/12.jpg");break;
            case 7:image = new Image("common_goal_cards/4.jpg");break;
            case 8:image = new Image("common_goal_cards/1.jpg");break;
            case 9:image = new Image("common_goal_cards/5.jpg");break;
            case 10:image = new Image("common_goal_cards/2.jpg");break;
            case 11:image = new Image("common_goal_cards/6.jpg");break;
            case 12:image = new Image("common_goal_cards/10.jpg");break;
        }
        if(n==1){
            CG1.setImage(image);
        }
        if(n==2){
            CG2.setImage(image);
        }

    }

}
