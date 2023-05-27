package GUI;

import GUI.PositionStuff.Position;
import Server.RMI.RMIinterface;
import Server.Socket.ClientSocket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import javafx.scene.input.MouseEvent;

import java.rmi.RemoteException;
import java.util.*;

public class GameSceneController {
    @FXML
    public Text TopLabel;
    @FXML
    public GridPane Opp1ShelfGrid;
    @FXML
    private AnchorPane GameAnchor;
    @FXML
    public GridPane Opp2ShelfGrid;
    @FXML
    public GridPane Opp3ShelfGrid;
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
    @FXML
    public ImageView MyEndGameToken;
    @FXML
    public ImageView MyToken2;
    @FXML
    public ImageView EndGameToken1;
    @FXML
    public ImageView EndGameToken2;
    @FXML
    public ImageView EndGameToken3;

    public boolean drawIsOver;
    public GridPane PlayerShelfGrid;
    public Label PlayerName;
    private int drawnTilesCounter;
    private List<Position> alreadyDrawnPositions;
    private boolean leaderboardCheck = false;
    private ClientNotificationRMIGUI clientRMI;
    private ClientSocket clientSocket;
    public void setClient(ClientNotificationRMIGUI client) {
        this.clientRMI = client;
        //System.out.println(this.toString());
        //System .out.println("AAAAAAAAAAAAAAAAAAAAAA");
        clientRMI.setGameSceneController(this);
    }
    public void setClient(ClientSocket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void setPlayerName(String name){
        Platform.runLater(() -> PlayerName.setText(name));
    }
    public ClientNotificationRMIGUI getClientRMI(){
        return this.clientRMI;
    }


    /**
     * Updates the GUI showing game's first turn board, PersonalGoalCard, CommonGoalCards and leaderbaord
     */
    public void updateGUIAtBeginningOfGame(TilePlacingSpot[][] board, Map<Integer, PersonalGoalCard> pgcMap, PersonalGoalCard pgc, List<CommonGoalCard> cgcs, List<Player> leaderboard, String isPlaying){
        Platform.runLater(() -> {
            //TODO refactor code below (chat setting) to a private method
            messageTextArea2.setVisible(true);
            messageTextArea2.setText("Welcome to My Shelfie! To chat with others just type in the box below, to chat privately with another player type @NameOfPlayer followed by the message you wish to send");
            messageTextArea2.appendText("\n");
            chatButton.setVisible(true);
            chatTextField.setVisible(true);
           updateBoard(board);
           setPersonalGoalCardImage(pgc, pgcMap);
           createLeaderboard(leaderboard);
           setCommonGoalCardImage(cgcs.get(0),1);
           setCommonGoalCardImage(cgcs.get(1),2);
           setPlayerLabels();
           updateTurnLabel(isPlaying);
           createShelfButtons();
           drawIsOver = false;
        });
    }

    /**
     * Replaces the scene's board tiles with grid[][] tiles. Each tile has a EventHandler<MouseEvent> to handle tile selection.
     * @param grid - game board
     */
    public void updateBoard(TilePlacingSpot[][] grid){
        alreadyDrawnPositions = new ArrayList<>();
        removeDrawnTilesFromBoard(grid);
        EventHandler<MouseEvent> eventHandler = e -> {
            Rectangle sender = (Rectangle) e.getSource();
            if((Integer)sender.getUserData()==1){
                removeTileFromDrawnTiles(e);
            }else {
                boardTileClicked(e);
            }
        };

        Image image;
        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){
                if (grid[i][j].isAvailable()) {
                    if(!grid[i][j].isEmpty()) {
                        Tile t = grid[i][j].showTileInThisPosition();
                        switch (t.getType()) {
                            case CAT -> {
                                image = new Image("item_tiles/Gatti1.1.png", 45, 45, true, true);
                                //System.out.print("C ");
                            }
                            case BOOK -> {
                                image = new Image("item_tiles/Libri1.1.png", 45, 45, true, true);
                                //System.out.print("B ");
                            }
                            case GAME -> {
                                image = new Image("item_tiles/Giochi1.1.png", 45, 45, true, true);
                                //System.out.print("G ");
                            }
                            case FRAME -> {
                                image = new Image("item_tiles/Cornici1.1.png", 45, 45, true, true);
                                //System.out.print("F ");
                            }
                            case PLANT -> {
                                image = new Image("item_tiles/Piante1.1.png", 45, 45, true, true);
                                //System.out.print("P ");
                            }
                            case TROPHY -> {
                                image = new Image("item_tiles/Trofei1.1.png", 45, 45, true, true);
                                //System.out.print("T ");
                            }
                            default -> image = null;
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

    /**
     * this method is used to remove the image of the tiles that has been drawn by a player from the board gui
     * @param boardView a matrix representing the current state of the board
     */
    public void removeDrawnTilesFromBoard(TilePlacingSpot[][] boardView){
        Platform.runLater(() -> {
            for(int i = 0;i<9;i++){
                for(int j=0;j<9;j++){
                    if(boardView[i][j].isEmpty()){
                        Node tileAtThisPosition = getNodeAt(i,j,BoardGrid);
                        if(tileAtThisPosition!=null){
                            //System.out.println("("+i+","+j+") dovrebbe essere eliminato");
                            BoardGrid.getChildren().remove(tileAtThisPosition);
                        }
                    }
                }
            }
        });
    }

    public void createLeaderboard(List<Player> leaderboard){
        Platform.runLater(() -> {
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
        });
    }

    /**
     * This method is used to update the leaderboard in the gui
     * @param leaderboard the List of players containing the leaderboard data
     */
    public void updateLeaderboard(List<Player> leaderboard){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final ObservableList<TableRecord> data = FXCollections.observableArrayList();
                for (Player p : leaderboard) {
                    data.add(new TableRecord(p.getNickname(), "" + p.getScore()));
                }
                TableLeaderboard.setItems(data);
            }
        });
    }

    /**
     * This method should be called at the end of a client's turn. It updates the board after
     * changes made by the client.
     */
    public void updateGameScene(String nextPlayer, TilePlacingSpot[][] board, List<Player> leaderboard, Shelf shelf){
        Platform.runLater(() -> {
           updateTurnLabel(nextPlayer);
           updateBoard(board);
           updateShelf(shelf);
           createLeaderboard(leaderboard);
           if(Objects.equals(clientSocket.getNickname(), clientSocket.isPlaying))
               drawIsOver = false;
        });
    }

    /**
     * Updates opponents' shelves
     * @param nickname the
     * @param grid
     */
    public void updateOppShelf(String nickname, Tile[][] grid){
        if(Opp1ShelfGrid.getUserData()!=null)
            if(((String)Opp1ShelfGrid.getUserData()).equals(nickname)){
            updateShelf(grid,Opp1ShelfGrid);
            }
        if(Opp2ShelfGrid.getUserData()!=null)
            if(((String)Opp2ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(grid,Opp2ShelfGrid);
            }
        if(Opp3ShelfGrid.getUserData()!=null)
            if(((String)Opp3ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(grid,Opp3ShelfGrid);
            }
    }

    public void updateOppShelf(String nickname, Shelf shelf){
        if(Opp1ShelfGrid.getUserData()!=null)
            if(((String)Opp1ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(shelf.getGrid(),Opp1ShelfGrid);
            }
        if(Opp2ShelfGrid.getUserData()!=null)
            if(((String)Opp2ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(shelf.getGrid(),Opp2ShelfGrid);
            }
        if(Opp3ShelfGrid.getUserData()!=null)
            if(((String)Opp3ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(shelf.getGrid(),Opp3ShelfGrid);
            }
    }


    /**
     * this method is used to handle the event on click of the tiles in the board, if the tile is not already been drawn then it's drawn
     * and placed in the TileToBeInserted grid pane, then the border of the drawn tile in the board turns to yellow
     * @param event event triggering the call of this method
     */
    private void boardTileClicked(Event event) {
        if(!Objects.isNull(clientRMI)){
            if(!clientRMI.isMyTurn())
                return;
        }else{
            if (!clientSocket.isMyTurn())
                return;
        }
        if(!drawIsOver){
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
                        //ImageView redx = new ImageView(new Image("game_stuff/x-mark.png"));
                        //redx.onMouseClickedProperty().set(this::removeTileFromDrawnTiles);
                        StackPane stackPane = new StackPane();
                        stackPane.getChildren().add(new ImageView(p.getImage()));
                        //stackPane.getChildren().add(redx);
                        //stackPane.setAlignment(redx, Pos.TOP_RIGHT);
                        TileToBeInserted.add(stackPane, getFirstEmptySpot(TileToBeInserted), 0);    //TODO Exception in thread "JavaFX Application Thread" java.lang.IllegalArgumentException: columnIndex must be greater or equal to 0, but was -1
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
    }

    /**
     * Sends the server a request to draw the selected tiles from the board.
     * Updates client's board.
     */
    private void handleCheckmarkButton(ActionEvent event) {
        Shelf shelf = Objects.isNull(clientSocket) ? new Shelf(clientRMI.getMyShelf()) : clientSocket.getShelf();

        if (alreadyDrawnPositions.size() > 0) {
            // grey out unavailable columns
            boolean atLeastOneColumnAvailable = false;
            for (int i = 0; i < 5; i++){
                if (!shelf.canItFit(drawnTilesCounter, i)) {
                    // grey out button
                    ((ImageView) shelfButtonsPane.getChildren().get(i)).setImage(new Image("misc/sort_down_gray.png"));
                    // find buttons and disable them
                    for (Node n: shelfButtonsPane.getChildren()){
                        if (n.getId() != null)
                            if (n.getId().equals(Integer.toString(i)))
                                n.setVisible(false);
                    }
                }
                else
                    atLeastOneColumnAvailable = true;
            }
            if (!atLeastOneColumnAvailable) return;

            // socket
            if (!Objects.isNull(clientSocket))
                socketHandleCheckmarkButton(event);
            // rmi
            else {
                if (!clientRMI.isMyTurn())
                    return;
                drawTilesFromRMIServer();
            }
            drawIsOver = true;
            TileToBeInserted.getChildren().remove(getNodeAt(0,3,TileToBeInserted));
        }
    }

    //TODO it should be that you cant press checkmark button if drawn tiles cant fit in any column

    /**
     * Sends the server a request to insert the tiles in the selected column of the shelf.
     * Calls updateGameScene().
     */
    private void handleShelfButton(ActionEvent e){
        // socket
        if (!Objects.isNull(clientSocket)) {
            if (!clientSocket.isMyTurn()) {
                //System.out.println("truee");
                return;
            }
            socketHandleShelfButton(e);
        }
        // rmi
        else {
            if (!clientRMI.isMyTurn())
                return;
            rmiHandleShelfButton(e);
        }

        //clean TileToBeInserted
        for (int column = 0; column < 3; column++) {
            TileToBeInserted.getChildren().remove(getNodeAt(0, column, TileToBeInserted));
        }

        //clean alreadyDrawnPositions
        alreadyDrawnPositions.clear();

        // hide shelf buttons and tile deck
        shelfButtonsPane.setVisible(false);
        //TileToBeInserted.setVisible(false);
        drawIsOver = false;
        drawnTilesCounter = 0;
        //reset greyed out buttons
        for (int i = 0; i < 5; i++)
            ((ImageView) shelfButtonsPane.getChildren().get(i)).setImage(new Image("misc/sort-down.png"));
    }

    /**
     * This method is called to insert the tile picked with the gui in the shelf on the rmi server
     * @param e
     */
    private void rmiHandleShelfButton(Event e){
        Button button = (Button) e.getSource();
        //System.out.println("Button getid: " + button.getId());
        int column = Integer.parseInt(button.getId());
        if(clientRMI.insertTilesInShelf(column));
            //System.out.println("Insert in shelf rmi successful");
        else;
            //System.out.println("Problem in insert in shelf");
        updateShelf(clientRMI.getMyShelf(),PlayerShelfGrid);
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
        if(clientRMI.drawTilesFromBoard(x,y,amount,direction)){
            //System.out.println("RMI draw operation was a success");

        }


        shelfButtonsPane.setVisible(true);
    }

    /**
     * this method is used to check wether a tile can be drawn from the board (is on the same row/column of the other drawn tiles,
     * is drawable according to game rules)
     * @param p it's the position of the tile in the board that we want to draw
     * @return true if the tile is drawable, false otherwise
     */
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
     * Updates player's shelf in the scene
     */
    private void updateShelf(Shelf shelf){
        for (int rows = 0; rows < 6; rows++)
            for (int columns = 0; columns < 5; columns++){
                Tile t = shelf.getGrid()[rows][columns];
                if (t != null){
                    ImageView img = (ImageView) getNodeAt(rows, columns, PlayerShelfGrid);
                    img.setImage(new Image(t.getImgPath(),45,45,true,true));
                }
            }
    }

    /**
     * Updates player's shelf in the scene
     * @param shelf a matrix of tile representing the state of the shelf
     */
    public void updateShelf(Tile[][] shelf, GridPane gridpane){
        Platform.runLater(() -> {
            for (int rows = 0; rows < 6; rows++)
                for (int columns = 0; columns < 5; columns++){
                    Tile t = shelf[rows][columns];
                    if (t != null){
                        ImageView img = (ImageView) getNodeAt(rows, columns, gridpane);
                        img.setImage(new Image(t.getImgPath(),45,45,true,true));
                    }
                }
        });

    }

    /**
     * Changes the top label to display the new currently playing client
     */
    public void updateTurnLabel(String player) {
        TopLabel.setText(player + "'s turn.");
    }

    /**
     * This method is used to find the first empty spot on the first row in a gridpane
     * @param gridPane the gridPane where we want to find the first empty spot
     * @return the column of the first row that is empty
     */
    private int getFirstEmptySpot(GridPane gridPane){
        for(int column =0; column<3; column++){
            if(getNodeAt(0,column,gridPane)==null)
                return column;
        }
        return -1;
    }

    /**
     * This method is used to remove a tile form the TileToBeInserted gridpane, this method is called by pressing on a tile in the board
     * that has the yellow border or on the red x on the tiles in the TileToBeInserted gridpane
     * it is not possible to remove the tile that is in the middle of other two
     * @param event
     */
    private void removeTileFromDrawnTiles(Event event){
        //System.out.println("remove tile is called ");
        if(!drawIsOver){
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

    }

    /**
     * This method is used to check if the tile in position p on the board is drawable according to the game rules (it must have at least one side free)
     * @param p position of the tile on which we are doing the control
     * @return true if the tile is drawable, false otherwise
     */
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

    public void endGamePopup(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Popup popup = new Popup();
                HBox buttons = new HBox(30);
                Button okButton = new Button("Ok");
                buttons.getChildren().add(okButton);
                Label message = new Label("Someone quit, so the game has ended");
                buttons.getChildren().add(message);
                buttons.setStyle("-fx-background-color: white; -fx-padding: 13px;");
                popup.getContent().add(buttons);
                //Stage stage = (Stage) GameAnchor.getScene().getWindow();
                popup.show(GameAnchor.getScene().getWindow());
                okButton.setOnAction(endgameEvent->{
                    Platform.exit();
                });

            }
        });
    }

    /**
     * This method is used to find the column of the tile to be removed from TileToBeInserted when we press a tile with yellow border on the board
     * @param p position of the tile on the board
     * @return the column in which the pressed tile is in the TileToBeInserted gridpane
     */
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

    /**
     * This method is used to get the Node in position (x, y) in the gridpane
     * @param row row of the element
     * @param column column of the element
     * @param gridPane gridpane in which we look for the node
     * @return null if the position (x,y) is empty in gridPane, otherwise it returns the node in that position
     */
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

    /**
     * this method is used to transform an integer into an int
     * @param i integer
     * @return the int value of the integer if the integer is not null, 0 otherwise
     */
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
                    while (!clientSocket.turnHasEnded) clientSocket.wait();    // waits for game to start
                }
                updateGUIAtBeginningOfGame(clientSocket.getBoard().getBoardForDisplay(), clientSocket.getPgcMap(), clientSocket.getPersonalGoalCard(), clientSocket.getCommonGoalCards(), clientSocket.getLeaderboard(), clientSocket.isPlaying);
                clientSocket.turnHasEnded = false;
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
                        while (!clientSocket.turnHasEnded) clientSocket.wait();
                    }
                    //System.out.println("Updated game scene");
                    //System.out.println(clientSocket.isPlaying);
                    updateGameScene(clientSocket.isPlaying, clientSocket.getBoard().getBoardForDisplay(), clientSocket.getLeaderboard(), clientSocket.getShelf());
                    clientSocket.turnHasEnded = false;
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
        ImageView[] endgametokens = new ImageView[]{EndGameToken1,EndGameToken2,EndGameToken3};
        int n;
        if(clientSocket!=null){
             n = clientSocket.getLeaderboard().size();
            for(int i=0;i<n;i++){
                if(!clientSocket.getLeaderboard().get(i).getNickname().equals(clientSocket.getNickname())){
                    labels[count].setText(clientSocket.getLeaderboard().get(i).getNickname());
                    shelfs[count].setImage(new Image("boards/bookshelf.png"));
                    if(clientSocket.getLeaderboard().get(i).hasEndGameToken())
                        endgametokens[i].setImage(new Image("scoring_tokens/endgame.jpg"));
                    count++;
                }
                if(clientSocket.getLeaderboard().get(i).getNickname().equals(clientSocket.getNickname()) && clientSocket.getLeaderboard().get(i).hasEndGameToken()){
                    MyEndGameToken.setImage(new Image("scoring_tokens/endgame.jpg"));
                }

            }
        }else{
            List<Player> players = clientRMI.getLeaderboard();
            n = clientRMI.getLeaderboard().size();
            for(int i=0;i<n;i++){
                if(!players.get(i).getNickname().equals(clientRMI.getNickname())){
                    labels[count].setText(players.get(i).getNickname());
                    shelfs[count].setImage(new Image("boards/bookshelf.png"));
                    switch(count){
                        case 0:{
                            Opp1ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(clientRMI.getShelfOfPlayer(players.get(i).getNickname()),Opp1ShelfGrid);
                        }break;
                        case 1:{
                            Opp2ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(clientRMI.getShelfOfPlayer(players.get(i).getNickname()),Opp1ShelfGrid);
                        }break;
                        case 2:{
                            Opp3ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(clientRMI.getShelfOfPlayer(players.get(i).getNickname()),Opp1ShelfGrid);
                        }break;
                    }
                    if(clientRMI.getLeaderboard().get(i).hasEndGameToken())
                        endgametokens[i].setImage(new Image("scoring_tokens/endgame.jpg"));
                    count++;
                }
                if(players.get(i).getNickname().equals(clientRMI.getNickname()) && players.get(i).hasEndGameToken()){
                    MyEndGameToken.setImage(new Image("scoring_tokens/endgame.jpg"));
                }
            }
        }



    }

    /**
     * This method is called in MatchTypeController and in LoginSceneController when the switch to GameScene is called to show incoming
     * messages to the player in messageTextArea
     * @author Diego Lecchi
     */
    public void socketMessageTextArea() {
        String message;
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

    /**
     * this method is called once chat button is pressed, if clientSocket != null it sends the chat message through method clientSpeaker
     * otherwise it means the player is using RMI so the message is sent through method sendChatMessage
     * @author Diego Lecchi
     * @param e
     * @throws RemoteException
     */
    public void chatButtonPressed(ActionEvent e) throws RemoteException {

        String message=chatTextField.getText();
        if(!Objects.equals(message, "")) {
            if(clientSocket != null)
                clientSocket.clientSpeaker("/chat " + message);
            else
                clientRMI.sendChatMessage(message);
            chatTextField.clear();

            Platform.runLater(() -> {
                messageTextArea2.appendText("YOU: " + message);
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

        // check if drawn tiles are horizontal or vertical
        for (Position alreadyDrawnPosition : alreadyDrawnPositions) {
            if (alreadyDrawnPosition.getX() != alreadyDrawnPositions.get(0).getX()) {
                horizontal = false;
                break;
            }
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
        clientSocket.clientSpeaker(Integer.toString(drawnTilesCounter));
        try {
            if (horizontal) {
                clientSocket.clientSpeaker("2");
                clientSocket.getBoard().drawTiles(min.getX(), min.getY(), drawnTilesCounter, Board.Direction.RIGHT);
            } else {
                clientSocket.clientSpeaker("1");
                clientSocket.getBoard().drawTiles(min.getX(), min.getY(), drawnTilesCounter, Board.Direction.DOWN);
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
        //System.out.println("Button getid: " + button.getId());
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
            //System.out.println("[GUI]" + clientSocket.gson.toJson(positionList));
            clientSocket.clientSpeaker("[GUI]" + clientSocket.gson.toJson(positionList));
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
           ex.printStackTrace();
        }
    }

    //****** end socket specific ********//


    /**
     * This method is called by ClientNotificationRMIGUI receiveMessage to show incoming messages to the player in messageTextArea
     * @author Diego Lecchi
     * @param message is the message received by the client from the server
     */
    public void rmiMessageTextArea(String message){
        if(!Objects.equals(message, "") && !Objects.equals(message, null)){
            Platform.runLater(() -> {
                messageTextArea2.appendText(message);
                messageTextArea2.appendText("\n");
            });

        }

    }
}
//TODO RMI currently can send but not recieve messages to/from socket
//TODO visualize common goal tokens in gui
//TODO socket should display error message when quitting

