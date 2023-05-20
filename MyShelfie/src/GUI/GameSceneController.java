package GUI;

import Server.Socket.ClientSocket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameSceneController {
    @FXML
    public Text TopLabel;

    private int drawnTilesCounter;

    private List<Position> alreadyDrawnPositions;
    class Position{
        private int x;
        private int y;
         public Position(int x,int y){
             this.x = x;
             this.y = y;
         }

         public int getX(){
             return x;
         }

        public int getY(){
            return y;
        }
    }
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
    private GridPane TileToBeInserted;

    @FXML
    private ImageView CG1;

    @FXML
    private ImageView CG2;
    private ClientNotificationRMIGUI clientRMI;
    private ClientSocket clientSocket;

    @FXML
    private Text CG1_id;

    @FXML
    private Text CG2_id;
    public void setClient(ClientNotificationRMIGUI client) {
        this.clientRMI = client;
        System.out.println(this.toString());
        //System .out.println("AAAAAAAAAAAAAAAAAAAAAA");
        clientRMI.setGameSceneController(this);
    }
    public void setClient(ClientSocket clientSocket){
        this.clientSocket = clientSocket;
    }
    public ClientNotificationRMIGUI getClientRMI(){
        return this.clientRMI;
    }


    /**
     * Updates the GUI showing game's first turn board, PersonalGoalCard, CommonGoalCards and leaderbaord
     */
    public void updateGUIAtBeginningOfGame(TilePlacingSpot[][] board, Map<Integer, PersonalGoalCard> pgcMap, PersonalGoalCard pgc, List<CommonGoalCard> cgcs, List<Player> leaderboard, String isPlaying){
        Platform.runLater(() -> {
           updateBoard(board);
           setPersonalGoalCardImage(pgc, pgcMap);
           createLeaderboard(leaderboard);
           setCommonGoalCardImage(cgcs.get(0),1);
           setCommonGoalCardImage(cgcs.get(1),2);
           updateTurnLabel(isPlaying);
        });
    }

    /**
     * Handles tile selection: mouse click on a board tile causes tile selection
     * @param grid - game board
     */
    public void updateBoard(TilePlacingSpot[][] grid){
        alreadyDrawnPositions = new ArrayList<>();
        drawnTilesCounter = 0;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        boardTileClicked(e);
                    }
                };

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
                                imv.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
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

    /**
     * This method is used to set the image of a common goal card
     * @param cgc the commongoal card
     * @param n the number of the common goal card (1,2)
     */
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
            CG1_id.setText(cgc.getDescription());
        }
        if(n==2){
            CG2.setImage(image);
            CG2_id.setText(cgc.getDescription());
        }

    }

    public void createLeaderboard(List<Player> leaderboard){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!leaderboardCheck) {
                    leaderboardCheck = true;
                    TableColumn player = new TableColumn("Player");
                    TableColumn score = new TableColumn("Score");
                    TableLeaderboard.getColumns().addAll(player, score);
                    TableLeaderboard.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                    final ObservableList<TableRecord> data = FXCollections.observableArrayList();
                    for (Player p : leaderboard) {
                        data.add(new TableRecord(p.getNickname(), "" + p.getScore()));
                    }

                    player.setCellValueFactory(new PropertyValueFactory<TableRecord, String>("player"));
                    score.setCellValueFactory(new PropertyValueFactory<TableRecord, String>("score"));

                    TableLeaderboard.setItems(data);
                }
            }
        });
    }

    /**
     * This method should be called at the end of a client's turn. It updates the board after
     * changes made by the client.
     */
    //TODO this needs to be called at the end of the turn in RMI
    public void updateGameScene(String nextPlayer, TilePlacingSpot[][] board, List<Player> leaderboard){
        Platform.runLater(() -> {
           updateTurnLabel(nextPlayer);
           updateBoard(board);
           createLeaderboard(leaderboard);
        });
    }


    private void boardTileClicked(Event event){
        if(drawnTilesCounter<3){
            //System.out.println(event.getSource().toString());
            ImageView sender = (ImageView) event.getSource();
            int row = transformIntegerToInt(GridPane.getRowIndex(sender));
            int column = transformIntegerToInt(GridPane.getColumnIndex(sender));
            if(alreadyDrawnPositions.stream().noneMatch(position -> (position.getX()==row && position.getY()==column))){
                alreadyDrawnPositions.add(new Position(row, column));
                ImageView tileSelected = new ImageView(sender.getImage());
                ImageView redx = new ImageView(new Image("game_stuff/x-mark.png"));
                redx.onMouseClickedProperty().set(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        removeTileFromDrawnTiles(e);
                    }
                });
                StackPane stackPane = new StackPane();
                stackPane.getChildren().add(tileSelected);
                stackPane.getChildren().add(redx);
                stackPane.setAlignment(redx, Pos.TOP_RIGHT);

                TileToBeInserted.add(stackPane,getFirstEmptySpot(TileToBeInserted) ,0);
                stackPane.setUserData(new Position(row, column));
                drawnTilesCounter++;
                if(drawnTilesCounter==1){
                    ImageView checkImg = new ImageView(new Image("game_stuff/check-mark.png"));
                    Button button = new Button();
                    button.setGraphic(checkImg);
                    TileToBeInserted.add(button,3,0);
                }
            }

        }
    }

    /**
     * Changes the top label to display the new currently playing client
     */
    private void updateTurnLabel(String player){
        TopLabel.setText(player + "'s turn.");
    }

    private int getFirstEmptySpot(GridPane gridPane){
        for(int column =0; column<3; column++){
            if(getNodeAt(0,column,gridPane)==null)
                return column;
        }
        return -1;
    }

    private void removeTileFromDrawnTiles(Event event){
        System.out.println("remove tile is called ");
        ImageView imv = (ImageView) event.getSource();
        StackPane stackPane = (StackPane) imv.getParent();
        if(TileToBeInserted.getChildren().remove(stackPane)){
            drawnTilesCounter--;
            Position p = (Position) stackPane.getUserData();
            alreadyDrawnPositions.remove(
                    alreadyDrawnPositions.stream().filter(
                            position -> (position.getX()==p.getX() && position.getY()==p.getY())
                    ).toList().get(0)
            );

        }

    }

    private Node getNodeAt(int row, int column, GridPane gridPane){
        Node result = null;
        ObservableList<Node> childrens = TileToBeInserted.getChildren();
        for (Node node : childrens) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    private int transformIntegerToInt(Integer i){
        if(i==null)
            return 0;
        else
            return i;
    }

    //****** socket specific ********//
    /**
     * Creates threads to run updateGUIIfGameHasStarted and socketUpdateGUI
     */
    public void runGameSceneThreads(){
        socketInitializeGameScene();
        socketUpdateGameScene();
    }
    /**
     * Used by socket to wait for server notification that game has started. When it has, updateGUIAtBeginningOfGame is called.
     * @throws InterruptedException
     */
    public void socketInitializeGameScene(){
        new Thread(() -> {
            try {
                synchronized (clientSocket) {
                    while (!clientSocket.areAllObjectsReceived()) clientSocket.wait();    // waits for game objects to be received from server
                }
                updateGUIAtBeginningOfGame(clientSocket.getBoard().getBoardForDisplay(), clientSocket.getPgcMap(), clientSocket.getPersonalGoalCard(), clientSocket.getCommonGoalCards(), clientSocket.getLeaderboard(), clientSocket.isPlaying);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }).start();
    }

    public void socketUpdateGameScene(){
        new Thread(() -> {
            // runs for the whole duration of the game
            while (!clientSocket.getSocket().isClosed()){
                try {
                    synchronized (clientSocket) {
                        while (clientSocket.isPlaying.equals("")) clientSocket.wait();
                    }
                    updateGameScene(clientSocket.isPlaying, clientSocket.getBoard().getBoardForDisplay(), clientSocket.getLeaderboard());
                    clientSocket.isPlaying = "";
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //****** end socket specific ********//
}
//TODO make application not resizable
//TODO visualize common goal in gui
//TODO update turn GUI