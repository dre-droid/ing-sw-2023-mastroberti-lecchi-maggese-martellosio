package GUI;

import GUI.PositionStuff.Position;
import Server.Socket.ClientSocket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import javafx.scene.input.MouseEvent;

import java.util.*;

public class GameSceneController {
    @FXML
    public Text TopLabel;
    public GridPane PlayerShelfGrid;

    private int drawnTilesCounter;

    private List<Position> alreadyDrawnPositions;
    private boolean leaderboardCheck = false;
    private ClientNotificationRMIGUI clientRMI;
    private ClientSocket clientSocket;
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

    @FXML
    private Text CG1_id;

    @FXML
    private Text CG2_id;

    @FXML
    private Label p1Label;
    @FXML
    private Label p2Label;
    @FXML
    private Label p3Label;
    @FXML
    private ImageView Opp1Shelf_ID;
    @FXML
    private ImageView Opp2Shelf_ID;
    @FXML
    private ImageView Opp3Shelf_ID;
    @FXML
    private Button chatButton;
    @FXML
    private TextArea messageTextArea2;
    @FXML
    private TextField chatTextField;
    @FXML
    public GridPane shelfButtonsPane;


    public void setClient(ClientNotificationRMIGUI client) {
        this.clientRMI = client;
        //System.out.println(this.toString());
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
           setPlayerLabels();
           updateTurnLabel(isPlaying);
           createShelfButtons();
        });
    }

    /**
     * Handles tile selection: mouse click on a board tile causes tile selection
     * @param grid - game board
     */
    public void updateBoard(TilePlacingSpot[][] grid){
        alreadyDrawnPositions = new ArrayList<>();
        drawnTilesCounter = 0;
        removeDrawnTilesFromBoard(grid);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        Rectangle sender = (Rectangle) e.getSource();
                        if((Integer)sender.getUserData()==1){
                            removeTileFromDrawnTiles(e);
                        }else {
                            boardTileClicked(e);
                        }
                    }
                };

                Image image;
                for(int i=0;i<9;i++){
                    for(int j=0;j<9;j++){
                        if (grid[i][j].isAvailable()) {
                            if(!grid[i][j].isEmpty()) {
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
                                ImagePattern imp = new ImagePattern(image);
                                Rectangle rect = new Rectangle();
                                rect.setUserData(0);
                                rect.setFill(imp);
                                rect.setHeight(42);
                                rect.setWidth(42);
                                //rect.setStyle("-fx-stroke: black; -fx-stroke-width: 5;");
                                rect.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
                                //imv.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
                                if(getNodeAt(i,j,BoardGrid)==null)
                                    BoardGrid.add(rect,j,i);
                                //imv.resize(imv.getFitHeight(), imv.getFitHeight());
                            }
                            else{
                                Rectangle rect = (Rectangle) getNodeAt(i,j,BoardGrid);

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
                //System.out.println("---"+id);
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
            default://.out.println("MANNAGGIAAA");
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

    public void removeDrawnTilesFromBoard(TilePlacingSpot[][] boardView){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i<9;i++){
                    for(int j=0;j<9;j++){
                        if(boardView[i][j].isEmpty()){
                            Node tileAtThisPosition = getNodeAt(i,j,BoardGrid);
                            if(tileAtThisPosition!=null){
                                System.out.println("("+i+","+j+") dovrebbe essere eliminato");
                                BoardGrid.getChildren().remove(tileAtThisPosition);
                            }
                        }
                    }
                }
            }
        });
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
    public void updateGameScene(String nextPlayer, TilePlacingSpot[][] board, List<Player> leaderboard, Shelf shelf){
        Platform.runLater(() -> {
           updateTurnLabel(nextPlayer);
           updateBoard(board);
           updateShelf(shelf);
           createLeaderboard(leaderboard);
        });
    }




    private void boardTileClicked(Event event) {
        if (drawnTilesCounter < 3) {
            //System.out.println(event.getSource().toString());
            Rectangle sender = (Rectangle) event.getSource();
            int row = transformIntegerToInt(GridPane.getRowIndex(sender));
            int column = transformIntegerToInt(GridPane.getColumnIndex(sender));
            //System.out.println("("+row+","+column+")");
            if (checkIfTileCanBeDrawn(new Position(row, column))) {
                if (alreadyDrawnPositions.stream().noneMatch(position -> (position.getX() == row && position.getY() == column))) {
                    sender.setStyle("-fx-stroke: yellow; -fx-stroke-width: 5;");
                    sender.setUserData(1);
                    alreadyDrawnPositions.add(new Position(row, column));
                    ImagePattern p = (ImagePattern) sender.getFill();
                    ImageView redx = new ImageView(new Image("game_stuff/x-mark.png"));
                    redx.onMouseClickedProperty().set(this::removeTileFromDrawnTiles);
                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().add(new ImageView(p.getImage()));
                    stackPane.getChildren().add(redx);
                    stackPane.setAlignment(redx, Pos.TOP_RIGHT);
                    TileToBeInserted.add(stackPane, getFirstEmptySpot(TileToBeInserted), 0);
                    stackPane.setUserData(new Position(row, column));
                    drawnTilesCounter++;
                    if (drawnTilesCounter == 1) {
                        ImageView checkImg = new ImageView(new Image("game_stuff/check-mark.png"));
                        Button checkmarkButton = new Button();
                        checkmarkButton.setGraphic(checkImg);
                        checkmarkButton.setOnAction(this::handleCheckmarkButton);
                        TileToBeInserted.add(checkmarkButton, 3, 0);
                    }
                }
            }
        }
    }

    /**
     * Sends the server a request to draw the selected tiles from the board.
     * Updates client's board.
     */
    private void handleCheckmarkButton(ActionEvent event) {
        if (alreadyDrawnPositions.size() > 0) {
            if (!Objects.isNull(clientSocket)) socketHandleCheckmarkButton(event);
            else {
                drawTilesFromRMIServer();
            }

        }
    }

    /**
     * Sends the server a request to insert the tiles in the selected column of the shelf.
     * Calls updateGameScene().
     */
    private void handleShelfButton(ActionEvent e){
        if (!Objects.isNull(clientSocket)) socketHandleShelfButton(e);
        else{//TODO RMI
        }
        shelfButtonsPane.setVisible(false);
    }

    private void createShelfButtons(){
        for (int i = 0; i < 5; i++) {
            Button shelfButton = new Button();
            shelfButton.setId(Integer.toString(i));
            shelfButton.setPrefSize(47, 47);
            shelfButton.setOpacity(0);
            shelfButton.setVisible(true);
            shelfButton.setOnAction(this::handleShelfButton);
            shelfButtonsPane.add(shelfButton, i, 0);
        }
    }

    /**
     * this method is used to send the tile to draw to the server, is called when the green check button is pressed
     */
    public void drawTilesFromRMIServer(){
        //first we get all the parameters we need from the gui
        int x, y, amount;
        Board.Direction direction;
        //we now have to check if the tiles are all on the same column or on the same row
        boolean onSameRow = true;
        x = alreadyDrawnPositions.get(0).getX();
        for(Position p: alreadyDrawnPositions){
            if(p.getX()!=x){
                onSameRow = false;
            }
        }//if onSameRow is true that means that the tiles are on the same row, if it is false then they are on the same column
        //we sort the list of position now
        if(onSameRow){
            alreadyDrawnPositions = alreadyDrawnPositions.stream().sorted(Comparator.comparingInt(Position::getY)).toList();
            /*for(int i=0;i<alreadyDrawnPositions.size();i++){
                System.out.println("("+alreadyDrawnPositions.get(i).getX()+","+alreadyDrawnPositions.get(i).getY()+")");
            }*/
            direction = Board.Direction.RIGHT;
        }
        else{
            alreadyDrawnPositions = alreadyDrawnPositions.stream().sorted(Comparator.comparingInt(Position::getX)).toList();
            direction = Board.Direction.DOWN;
        }
        x = alreadyDrawnPositions.get(0).getX();
        y = alreadyDrawnPositions.get(0).getY();
        amount = drawnTilesCounter;
        //System.out.println("coordinates: ("+x+","+y+")"+", amount = "+amount+", direction = "+direction);
        if(clientRMI.drawTilesFromBoard(x,y,amount,direction))
            System.out.println("RMI draw operation was a success");
    }

    private boolean checkIfTileCanBeDrawn(Position p){
        //sulla stessa riga delle altre tiles
        boolean returnValue= false;
        if(!checkIfTileIsDrawable(p))
            return false;
        if(drawnTilesCounter==0)
            return true;
        if(drawnTilesCounter==1){
            //adiacent to the already selected tile
            Position p1 = alreadyDrawnPositions.get(0);
            if(p1.getX()== p.getX()) {
                if(p.getY() == p1.getY() + 1 || p.getY() == p1.getY() - 1)
                    returnValue = true;
            }
            else if(p1.getY() == p.getY()){
                if(p.getX() == p1.getX() + 1 || p.getX() == p1.getX() - 1)
                    returnValue = true;
            }
            return returnValue;
        }
        if(drawnTilesCounter==2){

            Position p1 = alreadyDrawnPositions.get(0);
            Position p2 = alreadyDrawnPositions.get(1);
            //adiacent to the first tile
            if(p1.getX()== p.getX()) {
                if(p.getY() == p1.getY() + 1 || p.getY() == p1.getY() - 1)
                    returnValue = true;
            }
            else if(p1.getY() == p.getY()){
                if(p.getX() == p1.getX() + 1 || p.getX() == p1.getX() - 1)
                    returnValue = true;
            }
            //adiacent to the second tile
            if(p2.getX()==p1.getX()){
                if(p.getX()!=p2.getX())
                    return false;
            }
            if(p1.getY()==p2.getY()){
                if(p.getY()!=p2.getY())
                    return false;
            }
            if(p2.getX()== p.getX()) {
                if(p.getY() == p2.getY() + 1 || p.getY() == p2.getY() - 1)
                    returnValue = true;
            }
            else if(p2.getY() == p.getY()){
                if(p.getX() == p2.getX() + 1 || p.getX() == p2.getX() - 1)
                    returnValue = true;
            }
            if(p1.getX()==p2.getX()+2){
                if(p.getX()!=p2.getX()+1){
                    return false;
                }
            }
            if(p1.getX()==p2.getX()-2){
                if(p.getX()!=p1.getX()+1){
                    return false;
                }
            }
            if(p1.getY()==p2.getY()+2){
                if(p.getY()!=p2.getY()+1){
                    return false;
                }
            }
            if(p1.getY()==p2.getY()-2){
                if(p.getY()!=p1.getY()+1){
                    return false;
                }
            }
            return returnValue;
        }
        return false;
    }

    /**
     * Displays the argument shelf to the scene
     */
    private void updateShelf(Shelf shelf){
        for (int rows = 0; rows < 6; rows++)
            for (int columns = 0; columns < 5; columns++){
                Tile t = shelf.getGrid()[rows][columns];
                if (t != null){
                    System.out.println(t.getImgPath());
                    ImageView img = (ImageView) getNodeAt(rows, columns, PlayerShelfGrid);
                    img.setImage(new Image(t.getImgPath(),45,45,true,true));
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
        //System.out.println("remove tile is called ");
        try{
            ImageView sender = (ImageView) event.getSource();
            StackPane stackPane = (StackPane) sender.getParent();
            //System.out.println("tile counter: "+drawnTilesCounter);
            if(drawnTilesCounter==3){
                //if the tile that is being removed has two tiles selecte around it cannot be removed
                Position maybeMiddle = (Position) stackPane.getUserData();
                List<Position> otherPositions = new ArrayList<>();
                for(Position p: alreadyDrawnPositions){
                    if(!(p.getX()==maybeMiddle.getX() && p.getY()==maybeMiddle.getY())){
                        otherPositions.add(p);
                    }
                }
                for(Position p: otherPositions){
                    //System.out.println("("+p.getX()+","+p.getY()+")");
                }
                boolean allWithSameXs= true;
                int x=-1;
                for(Position p: alreadyDrawnPositions){
                    if(x==-1){
                        x=p.getX();
                    }else{
                        if(x!=p.getX())
                            allWithSameXs = false;
                    }
                }
                if(allWithSameXs){
                    //System.out.println("x uguali");
                    if(maybeMiddle.getY()==otherPositions.get(0).getY()+1 && maybeMiddle.getY()==otherPositions.get(1).getY()-1)
                        return;
                    if(maybeMiddle.getY()==otherPositions.get(0).getY()-1 && maybeMiddle.getY()==otherPositions.get(1).getY()+1)
                        return;
                }
                else{
                    //System.out.println("Y uguali");
                    if(maybeMiddle.getX()==otherPositions.get(0).getX()+1 && maybeMiddle.getX()==otherPositions.get(1).getX()-1)
                        return;
                    if(maybeMiddle.getX()==otherPositions.get(0).getX()-1 && maybeMiddle.getX()==otherPositions.get(1).getX()+1)
                        return;
                }
            }
            if(TileToBeInserted.getChildren().remove(stackPane)){
                drawnTilesCounter--;
                Position p = (Position) stackPane.getUserData();
                Rectangle rec = (Rectangle) getNodeAt(p.getX(), p.getY(), BoardGrid);
                rec.setUserData(0);
                rec.setStyle("-fx-stroke-width: 0;");
                alreadyDrawnPositions.remove(
                        alreadyDrawnPositions.stream().filter(
                                position -> (position.getX()==p.getX() && position.getY()==p.getY())
                        ).toList().get(0)
                );

            }
        }catch(ClassCastException e){
            //e.printStackTrace();
            Rectangle sender = (Rectangle) event.getSource();
            int row = transformIntegerToInt(GridPane.getRowIndex(sender));
            int column = transformIntegerToInt(GridPane.getColumnIndex(sender));
            if(drawnTilesCounter==3){
                //if the tile that is being removed has two tiles selecte around it cannot be removed
                Position maybeMiddle = new Position(row,column);
                List<Position> otherPositions = new ArrayList<>();
                for(Position p: alreadyDrawnPositions){
                    if(!(p.getX()==maybeMiddle.getX() && p.getY()==maybeMiddle.getY())){
                        otherPositions.add(p);
                    }
                }
                /*for(Position p: otherPositions){
                    System.out.println("("+p.getX()+","+p.getY()+")");
                }*/
                boolean allWithSameXs= true;
                int x=-1;
                for(Position p: alreadyDrawnPositions){
                    if(x==-1){
                        x=p.getX();
                    }else{
                        if(x!=p.getX())
                            allWithSameXs = false;
                    }
                }
                if(allWithSameXs){
                    //("x uguali");
                    if(maybeMiddle.getY()==otherPositions.get(0).getY()+1 && maybeMiddle.getY()==otherPositions.get(1).getY()-1)
                        return;
                    if(maybeMiddle.getY()==otherPositions.get(0).getY()-1 && maybeMiddle.getY()==otherPositions.get(1).getY()+1)
                        return;
                }
                else{
                    //System.out.println("Y uguali");
                    if(maybeMiddle.getX()==otherPositions.get(0).getX()+1 && maybeMiddle.getX()==otherPositions.get(1).getX()-1)
                        return;
                    if(maybeMiddle.getX()==otherPositions.get(0).getX()-1 && maybeMiddle.getX()==otherPositions.get(1).getX()+1)
                        return;
                }
            }

            sender.setUserData(0);
            sender.setStyle("-fx-stroke-width: 0;");
            //System.out.println(getColumnToRemoveTileFrom(new Position(row,column)));
            StackPane stackPane = (StackPane) getNodeAt(0, getColumnToRemoveTileFrom(new Position(row,column)), TileToBeInserted);
            //System.out.println(stackPane.toString());
            if(TileToBeInserted.getChildren().remove(stackPane)){
                //System.out.println("ok sono stato cancellato");
                drawnTilesCounter--;
                Position p = (Position) stackPane.getUserData();
                alreadyDrawnPositions.remove(
                        alreadyDrawnPositions.stream().filter(
                                position -> (position.getX()==p.getX() && position.getY()==p.getY())
                        ).toList().get(0)
                );

            }
        }
    }

    private boolean checkIfTileIsDrawable(Position p){
        boolean oneSideFree = false;
        int row, column;
        //check up
       // System.out.println("UP:");
        row = p.getX();
        column = p.getY()-1;
        if(getNodeAt(row, column, BoardGrid)==null){
            oneSideFree = true;
        }
        //check down
        //System.out.println("DOWN:");
        row = p.getX();
        column = p.getY()+1;
        if(getNodeAt(row, column, BoardGrid)==null){
            oneSideFree = true;
        }
        //check left
        //System.out.println("LEFT:");
        row = p.getX()-1;
        column = p.getY();
        if(getNodeAt(row, column, BoardGrid)==null){
            oneSideFree = true;
        }
        //check right
        //System.out.println("RIGHT:");
        row = p.getX()+1;
        column = p.getY();
        if(getNodeAt(row, column, BoardGrid)==null){
            oneSideFree = true;
        }
        return oneSideFree;
    }

    private int getColumnToRemoveTileFrom(Position p){
        ObservableList<Node> children = TileToBeInserted.getChildren();
        for(Node n: children){
            if(n.getUserData()!=null){
                Position np = (Position) n.getUserData();
                if(np.getX()==p.getX() && np.getY()==p.getY()){
                    //System.out.println("yeeeeeee");
                    return transformIntegerToInt(GridPane.getColumnIndex(n));
                }
            }
        }
        return -1;
    }

    private Node getNodeAt(int row, int column, GridPane gridPane){
        Node result = null;
        ObservableList<Node> childrens = gridPane.getChildren();
        //.out.println(gridPane.toString());
        for (Node node : childrens) {
            //System.out.println(node.toString());
            if(transformIntegerToInt(GridPane.getRowIndex(node)) == row && transformIntegerToInt(GridPane.getColumnIndex(node)) == column) {
                result = node;
                //System.out.println(result.toString());
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
                    updateGameScene(clientSocket.isPlaying, clientSocket.getBoard().getBoardForDisplay(), clientSocket.getLeaderboard(), clientSocket.getShelf());
                    clientSocket.isPlaying = "";
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setPlayerLabels(){
        int count = 0;
        Label[] labels = new Label[]{p1Label, p2Label, p3Label};
        ImageView[] shelfs = new ImageView[]{Opp1Shelf_ID,Opp2Shelf_ID,Opp3Shelf_ID};
        /*int n = clientSocket.getLeaderboard().size();
        for(int i=0;i<n;i++){
            if(!clientSocket.getLeaderboard().get(i).getNickname().equals(clientSocket.getNickname())){
                labels[count].setText(clientSocket.getLeaderboard().get(i).getNickname());
                shelfs[count].setImage(new Image("boards/bookshelf.png"));
                count++;
            }
        }*/

    }

    public void messageTextArea() {
        String message;
        messageTextArea2.setText("Welcome to My Shelfie! To chat with others just type in the box below, to chat privately with another player type @NameOfPlayer followed by the message you wish to send");
        messageTextArea2.appendText("\n");
        try {
            synchronized (clientSocket) {
                while (true) {
                    message = clientSocket.chatMessage;
                    if(!Objects.equals(message, "") && !Objects.equals(message, null)) {
                        String finalMessage = message;
                        Platform.runLater(() -> {
                            messageTextArea2.appendText(finalMessage);
                            messageTextArea2.appendText("\n");
                        });
                    }
                    clientSocket.wait();
                }
            }
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void chatButtonPressed(ActionEvent e){
        String message=chatTextField.getText();
        if(!Objects.equals(message, "")) {
            clientSocket.clientSpeaker("/chat " + message);
            chatTextField.clear();

            String finalMessage = message;
            Platform.runLater(() -> {
                messageTextArea2.appendText("YOU: " + finalMessage);
                messageTextArea2.appendText("\n");
            });
        }
    }

    /**
     * Sends socket server a request to draw the selected tiles. It mimics input from CLI.
     * Sets shelfButtonsPane to visible, draws tiles from board.
     */
    private void socketHandleCheckmarkButton(ActionEvent e){
        boolean horizontal = true;
        Position min;
        int size = alreadyDrawnPositions.size();

        // check if drawn tiles are horizontal or vertical
        for (int i = 0; i < alreadyDrawnPositions.size(); i++){
            if (alreadyDrawnPositions.get(i).getX() != alreadyDrawnPositions.get(0).getX()) horizontal = false;
        }

        // Find leftmost/topmost tile in alreadyDrawnPositions array, send socket its position. Update client's board.
        min = alreadyDrawnPositions.get(0);
        if (horizontal){
            for (Position p : alreadyDrawnPositions)
                if (p.getY() < min.getY()){
                    min = p;
                }
        } else {
            for (Position p : alreadyDrawnPositions)
                if (p.getX() < min.getX()){
                    min = p;
                }
        }
        clientSocket.clientSpeaker(Integer.toString(min.getX()));
        clientSocket.clientSpeaker(Integer.toString(min.getY()));
        clientSocket.clientSpeaker(Integer.toString(size));
        try {
            if (horizontal) {
                clientSocket.clientSpeaker("2");
                clientSocket.getBoard().drawTiles(min.getX(), min.getY(), size, Board.Direction.RIGHT);
            } else {
                clientSocket.clientSpeaker("1");
                clientSocket.getBoard().drawTiles(min.getX(), min.getY(), size, Board.Direction.RIGHT);
            }
        }catch (InvalidMoveException error){
            error.printStackTrace();
        }

        updateBoard(clientSocket.getBoard().getBoardForDisplay());
        shelfButtonsPane.setVisible(true);
    }

    /**
     * Sends server selected column and GUI message with serialized List<Position>
     */
    private void socketHandleShelfButton(ActionEvent e){
        // sends selected column
        Button button = (Button) e.getSource();
        System.out.println("Button getid: " + button.getId());
        if (!Objects.isNull(clientSocket)) {
            clientSocket.clientSpeaker(button.getId());
        }

        // Sends List<Position>, the positions of the selected tiles, to the server
        if (TileToBeInserted.getChildren().size() > 1) {
            List<Position> positionList = new ArrayList<>();
            for (Node n : TileToBeInserted.getChildren()) {
                Position p = (Position) n.getUserData();
                if (p != null) positionList.add(p);
            }
            System.out.println("[GUI]" + clientSocket.gson.toJson(positionList));
            clientSocket.clientSpeaker("[GUI]" + clientSocket.gson.toJson(positionList));
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        // wait to receive next turn info
        synchronized (clientSocket){

        }

        updateGameScene(clientSocket.isPlaying, clientSocket.getBoard().getBoardForDisplay(), clientSocket.getLeaderboard(), clientSocket.getShelf());
    }

    //****** end socket specific ********//
}
//TODO make application not resizable
//TODO visualize common goal tokens in gui
//TODO update turn GUI
