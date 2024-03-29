package main.java.it.polimi.ingsw.GUI;

import main.java.it.polimi.ingsw.GUI.PositionStuff.Position;
import main.java.it.polimi.ingsw.Server.Socket.GUISocket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

public class GameSceneController {

    @FXML
    public ImageView MyShelf;
    @FXML
    public GridPane RearrangeTiles;

    public ImageView firstPlayerSeat;
    @FXML
    public ImageView endGameToken;
    @FXML
    public Text TopLabel;
    @FXML
    public GridPane TokenContainer;
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

    @FXML ImageView cgc1tokens;

    @FXML ImageView cgc2tokens;
    @FXML ImageView OppChair1;
    @FXML ImageView OppChair2;
    @FXML ImageView OppChair3;

    private String nicknameHasEnded;
    public boolean drawIsOver;
    public GridPane PlayerShelfGrid;
    public Label PlayerName;
    private int drawnTilesCounter;
    private int maxDrawableTiles;

    private String nicknameFirstPlayer;
    private List<Position> alreadyDrawnPositions;
    private int[] reorderedList = new int[3];
    private boolean leaderboardCheck = false;
    private ClientNotificationRMIGUI clientRMI;
    private GUISocket clientSocket;

    public boolean alreadySet;


    public void setClient(ClientNotificationRMIGUI client) {
        this.clientRMI = client;
        //System.out.println(this.toString());
        clientRMI.setGameSceneController(this);
        chatSetUp();
        if(clientRMI.hasGameStarted()){
            clientRMI.updateGUIAtBeginningOfGame();
        }
    }
    public void setClient(GUISocket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void setPlayerName(String name){
        Platform.runLater(() -> PlayerName.setText(name));
    }
    public ClientNotificationRMIGUI getClientRMI(){
        return this.clientRMI;
    }


    /**
     * This method updates the GUI showing game's first turn board, PersonalGoalCard, CommonGoalCards and leaderbaord
     * @param board The game board
     * @param pgcMap The map of personal goal cards containing the valid tiles for comparison.
     * @param pgc The player's personal goal card
     * @param cgcs The list of common goal cards
     * @param leaderboard the list of players that are playing
     * @param isPlaying nickname of the player who is currently playing
     */
    public void updateGUIAtBeginningOfGame(TilePlacingSpot[][] board, Map<Integer, PersonalGoalCard> pgcMap, PersonalGoalCard pgc, List<CommonGoalCard> cgcs, List<Player> leaderboard, String isPlaying){
        Platform.runLater(() -> {
            for(Player player: leaderboard){
                if(player.hasFirstPlayerSeat()){
                    nicknameFirstPlayer= player.getNickname();
                }
            }
            updateBoard(board);
            setPersonalGoalCardImage(pgc, pgcMap);
            createLeaderboard(leaderboard);
            setCommonGoalCardImage(cgcs.get(0),1);
            setCommonGoalCardImage(cgcs.get(1),2);
            setPlayerLabels();
            updateTurnLabel(isPlaying);
            createShelfButtons();
            if (!Objects.isNull(clientRMI)) {
                updateShelf(clientRMI.getMyShelf(), PlayerShelfGrid);
                clientRMI.updateOpponentsShelf();
                updateLeaderboard(clientRMI.getLeaderboard());
                updateScoringTokens(clientRMI.getMyToken());
            }
            else{
                updateShelf(clientSocket.getShelf().getGrid(), PlayerShelfGrid);
                // update opp shelf
                for (Player p: clientSocket.getLeaderboard())
                    updateOppShelf(p.getNickname(), p.getShelf().getGrid());
                updateLeaderboard(clientSocket.getLeaderboard());
                updateScoringTokens(clientSocket.getScoringTokens());
                if (clientSocket.hasFirstPlayerSeat()) setFirstPlayerSeat();
            }
            updateCommonGoalCardTokens(1,cgcs.get(0).getScoringTokens());
            updateCommonGoalCardTokens(2, cgcs.get(1).getScoringTokens());
            drawIsOver = false;

            //this is used to quit the game when exiting the gui
            if(!alreadySet)
                setCloseAlert();

        });

    }

    /**
     * This method is used to display an alert when the player wants to close the GameScene window and checks
     * if the player wants to exit the game. If the player wants to end the game he will click the OK button in the alert
     * and the player will quit the game.
     */

    public void setCloseAlert(){
        alreadySet=true;
        Platform.runLater(()->{
            Stage st = (Stage) this.MyShelf.getScene().getWindow();
            st.setOnCloseRequest(e->{
                e.consume();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Do you want to quit the game?");
                    alert.setHeaderText("If you quit the game, the current game will end");

                    if(alert.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK){
                        if(clientRMI!=null){
                            clientRMI.quitGame();
                        }
                        else if(clientSocket!=null){
                            clientSocket.clientSpeaker("/quit");
                        }
                        st.close();
                        //Platform.exit();
                    }
                });
            });
        });
    }

    /**
     * Replaces the scene's board tiles with grid[][] tiles. Each tile has a EventHandler for MouseEvent to handle tile selection.
     * @param grid - game board
     */
    public void updateBoard(TilePlacingSpot[][] grid){
//
        Platform.runLater(() -> {
            //Calculates the available spaces in the emptiest column of the player shelf
            maxDrawableTiles = 0;
            Shelf shelf = Objects.isNull(clientSocket) ? new Shelf(clientRMI.getMyShelf()) : clientSocket.getShelf();
            for (int j = 1; j < 4; j++) {
                for (int i = 0; i < 5; i++) {
                    if (shelf.canItFit(j, i))
                        maxDrawableTiles = Integer.max(maxDrawableTiles, j);
                }
            }
            alreadyDrawnPositions = new ArrayList<>();
            removeDrawnTilesFromBoard(grid);
            EventHandler<MouseEvent> eventHandler = e -> {
                Rectangle sender = (Rectangle) e.getSource();
                if ((Integer) sender.getUserData() == 1) {
                    removeTileFromDrawnTiles(e);
                } else {
                    boardTileClicked(e);
                }
            };
            BoardGrid.getChildren().clear();
            Image image;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (grid[i][j].isAvailable()) {
                        if (!grid[i][j].isEmpty()) {
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
                            BoardGrid.add(rect, j, i);
                            //imv.resize(imv.getFitHeight(), imv.getFitHeight());
                        }
                    }
                }
                //System.out.println();
            }
        });
    }

    /**
     * This method is used to show the player his personal goal card.
     * @param pgc the personal goal card assigned to the player
     * @param pgcMap The map of personal goal cards containing the valid tiles for comparison.
     */

    public void setPersonalGoalCardImage(PersonalGoalCard pgc, Map<Integer, PersonalGoalCard> pgcMap){
        int id=-1;
        Image image=null;
        boolean flag;
        if(pgcMap==null || pgc ==null){
//
        }
        if(pgcMap.isEmpty()){
//
        }
        for(Map.Entry<Integer, PersonalGoalCard> pgcKey: pgcMap.entrySet()){

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

            }
            flag = true;


        }
//
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

    /**
     *  This method creates the leaderboard as a table with 2 columns: one for the player nickname and
     *  the other for their scores. The leaderboard is always sorted in decreasing points.
     * @param leaderboard the list of player who are playing and their scores
     */

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
                    if(p.hasEndGameToken()){
                        System.out.println(""+p.getNickname()+" has the end game token");
                        nicknameHasEnded = p.getNickname();
                    }
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
            if(clientSocket != null) {
                if (clientSocket.getNickname().equals(nicknameHasEnded)){
                    //nicknameHasEnded=clientSocket.getNickname();
                    setEndGameToken();
                    Optional<Player> endgameholder = clientSocket.getLeaderboard().stream().filter(player -> player.hasEndGameToken()).findFirst();
                    endgameholder.ifPresent(player -> setOpponentEndGameToken(player.getNickname()));
                }
            }else{
                if(clientRMI != null){
                    if(clientRMI.hasEndgameToken(clientRMI.getNickname())){
                        nicknameHasEnded=clientSocket.getNickname();
                        setEndGameToken();
                    }
                }
            }
            // update scoring tokens on common goal cards
            for (int i = 0; i < clientSocket.getCommonGoalCards().size(); i++)
                updateCommonGoalCardTokens(i + 1, clientSocket.getCommonGoalCards().get(i).getScoringTokens());
            updateScoringTokens(clientSocket.getScoringTokens());
            // update opponents shelves
            for (Player p: leaderboard){
                updateOppShelf(p.getNickname(), p.getShelf().getGrid());
            }

            updateLeaderboard(leaderboard);
            if(Objects.equals(clientSocket.getNickname(), clientSocket.isPlaying))
                drawIsOver = false;
        });
    }

    /**
     * This method updates Opponent Shelfs and assigns firstplayertoken and endgame token checking
     * the correspondence between the UserData and the nickname of the player that has those tokens
     * @param nickname : the nickname of player whose shelf is updated
     * @param grid : Shelf's grid from which you retrieve user data.
     */
    public void updateOppShelf(String nickname, Tile[][] grid){
        if(Opp1ShelfGrid.getUserData()!=null)
            if((Opp1ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(grid,Opp1ShelfGrid);
                if(nickname.equals(nicknameFirstPlayer)){
                    OppChair1.setImage(new Image("misc/firstplayertoken.png"));
                }
                if(nickname.equals(nicknameHasEnded)){
                    EndGameToken1.setImage(new Image("scoring_tokens/endgame.jpg"));
                }
            }
        if(Opp2ShelfGrid.getUserData()!=null)
            if((Opp2ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(grid,Opp2ShelfGrid);
                if(nickname.equals(nicknameFirstPlayer)){
                    OppChair2.setImage(new Image("misc/firstplayertoken.png"));
                }
                if(nickname.equals(nicknameHasEnded)){
                    EndGameToken2.setImage(new Image("scoring_tokens/endgame.jpg"));
                }
            }
        if(Opp3ShelfGrid.getUserData()!=null)
            if((Opp3ShelfGrid.getUserData()).equals(nickname)){
                updateShelf(grid,Opp3ShelfGrid);
                if(nickname.equals(nicknameFirstPlayer)){
                    OppChair3.setImage(new Image("misc/firstplayertoken.png"));
                }
                if(nickname.equals(nicknameHasEnded)){
                    EndGameToken3.setImage(new Image("scoring_tokens/endgame.jpg"));
                }
            }
    }

    /**
     * This method is used to update opponentEndGameToken
     * @param playerHoldingToken : nickname of the player who is holding the endgame token.
     */

    public void setOpponentEndGameToken(String playerHoldingToken){
        Image endGameToken = new Image("scoring_tokens/endgame.jpg");
        if(Opp1ShelfGrid.getUserData()!=null){
            if(playerHoldingToken.equals(Opp1ShelfGrid.getUserData())){
                EndGameToken1.setImage(endGameToken);
            }
        }
        if(Opp2ShelfGrid.getUserData()!=null){
            if(playerHoldingToken.equals(Opp2ShelfGrid.getUserData())){
                EndGameToken2.setImage(endGameToken);
            }
        }
        if(Opp3ShelfGrid.getUserData()!=null){
            if(playerHoldingToken.equals(Opp3ShelfGrid.getUserData())){
                EndGameToken3.setImage(endGameToken);
            }
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
            if (!clientSocket.isMyTurn() || !clientSocket.youCanDraw)
                return;
        }
        if(!drawIsOver){
            if (drawnTilesCounter < Integer.min(maxDrawableTiles, 3)) {
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
                        //sort of the already drawn positions

                        ImagePattern p = (ImagePattern) sender.getFill();

                        StackPane stackPane = new StackPane();
                        stackPane.getChildren().add(new ImageView(p.getImage()));

                        TileToBeInserted.add(stackPane, getFirstEmptySpot(TileToBeInserted).getY(), 0);
                        stackPane.setUserData(new Position(row, column));
                        drawnTilesCounter++;
                        updateTileToBeInserted(alreadyDrawnPositions);


                    }
                }
            }
        }
    }

    /**
     * This method is used to sort the elements in the gridpane TileToBeInserted based on their position on the board
     * (if they were on the same y, they are sorted with ascending x value, if they were on the same x with ascending
     * x value)
     * @param positions
     */
    private void updateTileToBeInserted(List<Position> positions){
        positions = Position.sortPositions(positions);
        //we save the images and the positions in two lists with corresponding index
        List<Position> positionList = new ArrayList<>();
        List<StackPane>stackPaneList = new ArrayList<>();
        int i=0;
        for(int column=0;column<3;column++){
            if(getNodeAt(0,column, TileToBeInserted)!=null){
                StackPane n = (StackPane) getNodeAt(0,column,TileToBeInserted);
                Position pos = (Position) n.getUserData();
                //System.out.println("round "+i+", "+pos.toString()+"//"+n.toString());
                positionList.add(i,pos);
                stackPaneList.add(i,n);
                i++;
            }

        }

        TileToBeInserted.getChildren().clear();
        for(Position p: positions){
            TileToBeInserted.add(stackPaneList.get(positionList.indexOf(p)), getFirstEmptySpot(TileToBeInserted).getY(), 0);
        }
        if (drawnTilesCounter >= 1) {
            ImageView checkImg = new ImageView(new Image("game_stuff/check-mark.png"));
            Button checkmarkButton = new Button();
            checkmarkButton.setGraphic(checkImg);
            checkmarkButton.setOnAction(this::handleCheckmarkButton);
            TileToBeInserted.add(checkmarkButton, 3, 0);
        }
    }




    /**
     * This method sends the server a request to draw the selected tiles from the board.
     * Updates client's board.
     * @param event the CheckMarkButton has been pressed
     */
    private void handleCheckmarkButton(ActionEvent event) {
        Shelf shelf = Objects.isNull(clientSocket) ? new Shelf(clientRMI.getMyShelf()) : clientSocket.getShelf();

        if (alreadyDrawnPositions.size() > 0) {
            // grey out unavailable columns
            boolean atLeastOneColumnAvailable = false;
            for (int i = 0; i < 5; i++){
                if (!shelf.canItFit(drawnTilesCounter, i)) {
                    // grey out button
                    ImageView img = ((ImageView) shelfButtonsPane.getChildren().get(i));
                    img.setImage(new Image("misc/sort_down_gray.png"));
                    img.setOnMouseClicked(e -> {}); // do nothing
                }
                else
                    atLeastOneColumnAvailable = true;
            }
            if (!atLeastOneColumnAvailable) {
                for (int i = 0; i < 5; i++) {
                    ImageView img = (ImageView) shelfButtonsPane.getChildren().get(i);
                    img.setImage(new Image("misc/sort-down.png"));
                    img.setOnMouseClicked(shelfButtonsHandler);
                }
                return;
            }
            shelfButtonsPane.setVisible(true);

            // socket
            if (!Objects.isNull(clientSocket))
                socketHandleCheckmarkButton(event);
                // rmi
            else {
                if (!clientRMI.isMyTurn())
                    return;
                drawTilesFromRMIServer();
            }
            if(alreadyDrawnPositions!=null)
                drawIsOver = true;
            // hide checkmark button
            TileToBeInserted.getChildren().remove(getNodeAt(0,3,TileToBeInserted));

            //show arrows to rearrange tiles
            int numOfArrows = drawnTilesCounter*2;
            for(int i=0;i<numOfArrows;i++){
                ImageView arrowContainer = new ImageView();
                Image arrow;
                int finalI = i/2;
                if(i%2==0){
                    //left arrow
                    arrow = new Image("game_stuff/left_arrow.png", 30,30,true, false);
                    arrowContainer.setImage(arrow);
                    EventHandler<MouseEvent> eventHandlerLeft = e -> {
                        switchDrawnTiles(finalI, finalI -1);
                    };
                    arrowContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandlerLeft);
                }
                else{
                    //right arrow
                    arrow = new Image("game_stuff/right_arrow.png", 30,30,true, false);
                    arrowContainer.setImage(arrow);
                    EventHandler<MouseEvent> eventHandlerRight = e -> {
                        switchDrawnTiles(finalI, finalI +1);
                    };
                    arrowContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandlerRight);
                }
                RearrangeTiles.add(arrowContainer, i, 0);


            }
        }
    }

    /**
     * This method is used to switch the two elements in the gridpane TileTobBeInserted
     * @param pos1 column of the first element to swap
     * @param pos2 column of the second element to swap
     */
    public /*synchronized*/ void switchDrawnTiles(int pos1, int pos2){
        if(pos1>=0 && pos1<drawnTilesCounter && pos2>=0 && pos2<drawnTilesCounter){
            Node temp1 = getNodeAt(0, pos1, TileToBeInserted);
            Node temp2 = getNodeAt(0, pos2, TileToBeInserted);
            Position p1 = (Position) temp1.getUserData();
            Position p2 = (Position) temp2.getUserData();
            TileToBeInserted.getChildren().remove(getNodeAt(0, pos1, TileToBeInserted));
            TileToBeInserted.getChildren().remove(getNodeAt(0, pos2, TileToBeInserted));
            TileToBeInserted.add(temp1, pos2, 0);
            TileToBeInserted.add(temp2, pos1, 0);
            getNodeAt(0, pos1, TileToBeInserted).setUserData(p2);
            getNodeAt(0, pos2, TileToBeInserted).setUserData(p1);

            // socket reordered list
            int temp = reorderedList[pos2];
            reorderedList[pos2] = reorderedList[pos1];
            reorderedList[pos1] = temp;

            if(clientRMI!=null){
                clientRMI.rearrangeDrawnTiles(pos1, pos2);
            }
        }
    }

    /**
     * This method sends the server a request to insert the tiles in the selected column of the shelf.
     * Calls updateGameScene().
     * @param column the column in which you want to insert the tiles drawn
     */
    private void handleShelfButton(String column){
        // socket
        if (!Objects.isNull(clientSocket)) {
            if (!clientSocket.isMyTurn()) {
                return;
            }
            socketHandleShelfButton(column);
        }
        // rmi
        else {
            if (!clientRMI.isMyTurn())
                return;
            rmiHandleShelfButton(column);
        }

        //clean TileToBeInserted
        for (int i = 0; i < 4; i++) {
            TileToBeInserted.getChildren().remove(getNodeAt(0, i, TileToBeInserted));
        }

        //clean alreadyDrawnPositions
        alreadyDrawnPositions.clear();

        // hide shelf buttons and tile deck
        shelfButtonsPane.setVisible(false);
        //TileToBeInserted.setVisible(false);
        drawIsOver = false;
        drawnTilesCounter = 0;
        //reset greyed out buttons
        for (int i = 0; i < 5; i++) {
            ImageView img = (ImageView) shelfButtonsPane.getChildren().get(i);
            img.setImage(new Image("misc/sort-down.png"));
            img.setOnMouseClicked(shelfButtonsHandler);
        }
        //reset rearrange buttons
        RearrangeTiles.getChildren().clear();
    }

    /**
     * This method is called to insert the tile picked with the gui in the shelf on the rmi server
     * @param column : the column in which you want to insert the tiles drawn.
     */
    private void rmiHandleShelfButton(String column){
        if(clientRMI.insertTilesInShelf(Integer.parseInt(column)));
            //System.out.println("Insert in shelf rmi successful");
        else;
        //System.out.println("Problem in insert in shelf");
        if(clientRMI.getMyShelf()!=null)
            updateShelf(clientRMI.getMyShelf(),PlayerShelfGrid);
    }

    EventHandler<MouseEvent> shelfButtonsHandler = e -> {
        ImageView img = (ImageView) e.getSource();
        handleShelfButton(img.getId());
    };

    /**
     * This method is used th set Shelf Buttons visible when the green check has been pressed and the player has to insert
     * the tiles in the shelf.
     */
    private void createShelfButtons(){
        shelfButtonsPane.setVisible(false);
        for (int i = 0; i < 5; i++) {
            ImageView img = (ImageView) shelfButtonsPane.getChildren().get(i);
            img.setId(Integer.toString(i));
            img.setVisible(true);
            img.setOnMouseClicked(shelfButtonsHandler);
        }
    }

    /**
     * This method draws tiles from the RMI server and updates the game board.
     * Retrieves the necessary parameters from the GUI and checks if the drawn tiles are on the same row or column.
     * It sorts the list of positions accordingly and determines the direction in which the tiles should be placed on the game board.
     * The method then calls the RMI server's drawTilesFromBoard(int, int, int, Board.Direction) method
     * to draw the tiles and updates the game board accordingly.
     * If the draw operation is successful, the shelf buttons pane is made visible on the GUI.
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
            shelfButtonsPane.setVisible(true);
        }



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
     * This method is used to update the player's shelf in Game Scene
     * @param shelf : the shelf to be updated
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
     * This method updates the turnLabel with the nickname of the player who is currently playing
     * @param player the nickname of the player who is playing
     */
    public void updateTurnLabel(String player) {
        TopLabel.setText(player + "'s turn.");
    }

    /**
     * This method is used to find the first empty spot in a gridpane
     * @param gridPane the gridPane where we want to find the first empty spot
     * @return th
     */
    private Position getFirstEmptySpot(GridPane gridPane){
        for(int row=0;row<gridPane.getRowCount();row++)
            for(int column =0; column<gridPane.getColumnCount(); column++){
                if(getNodeAt(row,column,gridPane)==null){
                    return new Position(row, column);
                }
//
            }
        return null;
    }

    /**
     * This method is used to remove a tile form the TileToBeInserted gridpane, this method is called by pressing on a tile in the board
     * that has the yellow border or on the red x on the tiles in the TileToBeInserted gridpane
     * it is not possible to remove the tile that is in the middle of other two
     * @param event
     */
    private void removeTileFromDrawnTiles(Event event){
        if(!drawIsOver){
            Rectangle sender = (Rectangle) event.getSource();
            int row = transformIntegerToInt(GridPane.getRowIndex(sender));
            int column = transformIntegerToInt(GridPane.getColumnIndex(sender));
            Position rmvPos = new Position(row,column);
            if(drawnTilesCounter==3){
                //if the tile that is being removed has two tiles selected around it cannot be removed
                for(int i=0;i<3;i++){
                    StackPane sp = (StackPane) getNodeAt(0, i, TileToBeInserted);
                    Position checkPos = (Position) sp.getUserData();
                    if(rmvPos.equals(checkPos)){
                        if(i==1){
                            return;
                        }
                    }
                }
            }
            //we mark the tile on the board as already drawn
            sender.setUserData(0);
            sender.setStyle("-fx-stroke-width: 0;");
            //we now remove the tile from TileToBeInserted
            StackPane stackPane = (StackPane) getNodeAt(0, getColumnToRemoveTileFrom(new Position(row,column)), TileToBeInserted);
            if(TileToBeInserted.getChildren().remove(stackPane)){
                drawnTilesCounter--;
                Position p = (Position) stackPane.getUserData();
                alreadyDrawnPositions.stream().filter(
                        position -> (position.getX()==p.getX() && position.getY()==p.getY())
                ).findFirst().ifPresent(position->alreadyDrawnPositions.remove(position));
                updateTileToBeInserted(alreadyDrawnPositions);
            }
        }

    }

  /*  private void updateCG(List<CommonGoalCard> cgcs, int i){
        if(clientSocket != null){
            for(Player p: clientSocket.getLeaderboard()){
                int num = clientSocket.getLeaderboard().size();
                if(cgcs.get(i).isSatisfiedBy(p) && !cgcs.get(i).hasAlredyBeenRewarded(p)){
                    switch (cgcs.get(i).getTokens().get(cgcs.get(i).getTokens().size()-1).getPoints()){

                    }
                }
            }
        }

    }
*/




    /**
     * This method is used to check if the tile in position p on the board is drawable according to the game rules (it must have at least one side free)
     * @param p position of the tile on which we are doing the control
     * @return true if the tile is drawable, false otherwise
     */
    private boolean checkIfTileIsDrawable(Position p){
        boolean oneSideFree = false;
        int row, column;
        //check up
        row = p.getX();
        column = p.getY()-1;
        if(getNodeAt(row, column, BoardGrid)==null){
            oneSideFree = true;
        }
        //check down
        row = p.getX();
        column = p.getY()+1;
        if(getNodeAt(row, column, BoardGrid)==null){
            oneSideFree = true;
        }
        //check left
        row = p.getX()-1;
        column = p.getY();
        if(getNodeAt(row, column, BoardGrid)==null){
            oneSideFree = true;
        }
        //check right

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
        ObservableList<Node> children = gridPane.getChildren();
        //.out.println(gridPane.toString());
        for (Node node : children) {
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
     * This method is used to transform an integer into an int
     * @param i integer to trasfrom
     * @return the int value of the integer if the integer is not null, 0 otherwise
     */
    private int transformIntegerToInt(Integer i){
        if(i==null)
            return 0;
        else
            return i;
    }

    //****** socket specific ********//
    private final Object gameStartLock = new Object();

    /**
     * Creates threads to run updateGUIIfGameHasStarted and socketUpdateGUI
     */
    public void runGameSceneThreads(){
        chatSetUp();
        socketInitializeGameScene();
        socketUpdateGameScene();
    }

    /**
     * Used by socket to wait for server notification that game has started. When it has, updateGUIAtBeginningOfGame is called.
     */
    public void socketInitializeGameScene(){
        synchronized (gameStartLock) {  // simply avoids both the socketInitializeGameScene and the socketUpdateGameScene threads execute simultaneously
            new Thread(() -> {
                try {
                        /*synchronized (clientSocket) {
                            while (!clientSocket.turnHasEnded) clientSocket.wait();    // waits for game to start
                        }

                         */
                    synchronized (clientSocket.turnHasEndedLock){
                        while (!clientSocket.turnHasEnded)
                            clientSocket.turnHasEndedLock.wait();
                    }
                    if (clientSocket.gameEnd) switchToEndGameScene(clientSocket.getLeaderboard());
                    updateGUIAtBeginningOfGame(clientSocket.getBoard().getBoardForDisplay(), clientSocket.getPgcMap(), clientSocket.getPersonalGoalCard(), clientSocket.getCommonGoalCards(), clientSocket.getLeaderboard(), clientSocket.isPlaying);
                    setPlayerName(clientSocket.getNickname());
                    clientSocket.turnHasEnded = false;
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /** This method updates the game scene via socket communication.
     *  This method runs in a separate thread and continuously updates the game scene based on incoming data received via socket communication.
     *  It synchronizes access to ensure that the updateGameScene method is not executed simultaneously
     *  with the socketInitializeGameScene() method.
     *  The method waits for the turn to end, and upon receiving the notification, updates the game scene with the current state of the board,
     *  leaderboard, and shelf. If the game ends, it triggers the switch to the end game scene by passing the leaderboard data.
     *  Additionally, if the connection is lost, it navigates back to the login screen by calling the {@link #backToLogin()} method.
     */
    public void socketUpdateGameScene(){
        new Thread(() -> {
            // runs for the whole duration of the game
            synchronized (gameStartLock){   // simply avoids both the socketInitializeGameScene and the socketUpdateGameScene threads execute simultaneously
                while (!clientSocket.getSocket().isClosed()) {
                    try {
                        /*synchronized (clientSocket) {
                            while (!clientSocket.turnHasEnded) clientSocket.wait();
                        }

                         */
                        synchronized (clientSocket.turnHasEndedLock){
                            while (!clientSocket.turnHasEnded)
                                clientSocket.turnHasEndedLock.wait();
                        }
                        if (clientSocket.gameEnd) switchToEndGameScene(clientSocket.getLeaderboard());
                        updateGameScene(clientSocket.isPlaying, clientSocket.getBoard().getBoardForDisplay(), clientSocket.getLeaderboard(), clientSocket.getShelf());
                        clientSocket.turnHasEnded = false;
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        //thread to assign endGameToken for socket
        new Thread(() -> {
            synchronized (clientSocket.endGameTokenNickLock){
                while(clientSocket.endGameTokenNick.equals("")) {
                    try {
                        clientSocket.endGameTokenNickLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            nicknameHasEnded = clientSocket.endGameTokenNick;
        }).start();
        //thread to launch endGameScene
        new Thread(() -> {
            synchronized (clientSocket.gameEndLock){
                while(!clientSocket.gameEnd) {
                    try {
                        clientSocket.gameEndLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            switchToEndGameScene(clientSocket.getLeaderboard());
        }).start();
        //thread to go back login if disconnected
        new Thread(() -> {
            synchronized (clientSocket.connectionLostLock){
                while(!clientSocket.connectionLost) {
                    try {
                        clientSocket.connectionLostLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                backToLogin();
            }
        }).start();
    }

    /**
     * @author SaverioMaggese99
     * This method set players tokens and shelfs at the beginning of the game and is called
     * in updateGUIatBeginningofGAme
     */
    public void setPlayerLabels(){
        int count = 0;
        Label[] labels = new Label[]{p1Label, p2Label, p3Label};
        ImageView[] shelfs = new ImageView[]{Opp1Shelf_ID,Opp2Shelf_ID,Opp3Shelf_ID};
        ImageView[] chairs = new ImageView[]{OppChair1,OppChair2,OppChair3};
        int n = clientSocket == null ? clientRMI.getLeaderboard().size() : clientSocket.getLeaderboard().size();
        List<Player> players = clientSocket == null ? clientRMI.getLeaderboard() : clientSocket.getLeaderboard();
        String nickname = clientSocket == null ? clientRMI.getNickname() : clientSocket.getNickname();

        for(int i=0;i<n;i++) {
            if (!players.get(i).getNickname().equals(nickname)) {
                labels[count].setText(players.get(i).getNickname());
                shelfs[count].setImage(new Image("boards/bookshelf.png"));
                // RMI switch
                if (clientSocket == null) {
                    switch (count) {
                        case 0 -> {
                            Opp1ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(clientRMI.getShelfOfPlayer(players.get(i).getNickname()), Opp1ShelfGrid);


                        }
                        case 1 -> {
                            Opp2ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(clientRMI.getShelfOfPlayer(players.get(i).getNickname()), Opp2ShelfGrid);

                        }
                        case 2 -> {
                            Opp3ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(clientRMI.getShelfOfPlayer(players.get(i).getNickname()), Opp3ShelfGrid);
                        }
                    }
                }
                // socket switch
                else {
                    switch (count) {
                        case 0 -> {
                            Opp1ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(players.get(i).getShelf().getGrid(), Opp1ShelfGrid);

                        }
                        case 1 -> {
                            Opp2ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(players.get(i).getShelf().getGrid(), Opp2ShelfGrid);
                        }
                        case 2 -> {
                            Opp3ShelfGrid.setUserData(players.get(i).getNickname());
                            updateShelf(players.get(i).getShelf().getGrid(), Opp3ShelfGrid);

                        }
                    }
                }

                count++;
                if (players.get(i).getNickname().equals(nickname) && players.get(i).hasEndGameToken())
                    MyEndGameToken.setImage(new Image("scoring_tokens/endgame.jpg"));
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
            synchronized (clientSocket.chatMessageLock) {
                while (true) {
                    message = clientSocket.chatMessage;
                    if(!Objects.equals(message, "") && !Objects.equals(message, null)) {
                        String finalMessage = message;
                        Platform.runLater(() -> {
                            messageTextArea2.appendText(finalMessage);
                            messageTextArea2.appendText("\n");
                        });
                    }
                    clientSocket.chatMessage = "";
                    clientSocket.chatMessageLock.wait();
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
     * @param e: chatButton clicked event
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
    private void socketHandleCheckmarkButton(ActionEvent e) {
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
        if (horizontal) {
            for (Position p : alreadyDrawnPositions)
                if (p.getY() < min.getY()) {
                    min = p;
                }
        } else {
            for (Position p : alreadyDrawnPositions)
                if (p.getX() < min.getX()) {
                    min = p;
                }
        }
        synchronized (clientSocket.drawLock){
            while(!clientSocket.draw){
                try {
                    clientSocket.drawLock.wait();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            clientSocket.draw = false;
        }
        clientSocket.clientSpeaker(Integer.toString(min.getX()));
        synchronized (clientSocket.drawLock){
            while(!clientSocket.draw){
                try {
                    clientSocket.drawLock.wait();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            clientSocket.draw = false;
        }
        clientSocket.clientSpeaker(Integer.toString(min.getY()));
        synchronized (clientSocket.drawLock){
            while(!clientSocket.draw){
                try {
                    clientSocket.drawLock.wait();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            clientSocket.draw = false;
        }
        clientSocket.clientSpeaker(Integer.toString(drawnTilesCounter));
        // send direction
        try {
            if (drawnTilesCounter > 1) {
                synchronized (clientSocket.drawLock){
                    while(!clientSocket.draw){
                        try {
                            clientSocket.drawLock.wait();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    clientSocket.draw = false;
                }
                if (horizontal) {
                    clientSocket.clientSpeaker("2");
                    clientSocket.getBoard().drawTiles(min.getX(), min.getY(), drawnTilesCounter, Board.Direction.RIGHT);
                } else {
                    clientSocket.clientSpeaker("1");
                    clientSocket.getBoard().drawTiles(min.getX(), min.getY(), drawnTilesCounter, Board.Direction.DOWN);
                }
            } else {
                clientSocket.getBoard().drawTiles(min.getX(), min.getY(), drawnTilesCounter, Board.Direction.RIGHT);
            }
            // should make drawing feel quicker by updating locally
            updateBoard(clientSocket.getBoard().getBoardForDisplay());
        } catch (InvalidMoveException error) {
            error.printStackTrace();
        }
        for (int i = 0; i < drawnTilesCounter; i++)
            reorderedList[i] = i + 1;
        shelfButtonsPane.setVisible(true);
        clientSocket.youCanDraw = false;
    }

    /**
     * This method sends server the selected column and information about reordering drawn tiles.
     * @param column the column sent to the server in which tiles have to be inserted.
     */
    private void socketHandleShelfButton(String column){
        // sends selected column
        int adjustedColumn = Integer.parseInt(column) + 1;
        synchronized (clientSocket.insertLock){
            while(!clientSocket.insert){
                try {
                    clientSocket.insertLock.wait();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            clientSocket.insert = false;
        }
        clientSocket.clientSpeaker(String.valueOf(adjustedColumn));

        // send the coordinates reordered by the player
        if (drawnTilesCounter > 1)
            for (int i = 0; i < drawnTilesCounter; i++) {
                System.out.println("DRAWN TILES COUNTER " + drawnTilesCounter);
                synchronized (clientSocket.insertLock){
                    while(!clientSocket.insert){
                        try {
                            clientSocket.insertLock.wait();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    clientSocket.insert = false;
                }
                clientSocket.clientSpeaker(String.valueOf(reorderedList[i]));
                System.out.println(String.valueOf(reorderedList[i]));
            }
    }

    //****** end socket specific ********//

    /**
     * This method is used to update the view of the scoring token of the player
     * @param tokens list of the scoring tokens
     */
    public void updateScoringTokens(List<ScoringToken> tokens){

        Platform.runLater(()->{
            if(tokens==null)
                return;
            TokenContainer.getChildren().clear();
            for(ScoringToken st: tokens){
                ImageView imv = new ImageView();
                switch(st.getPoints()){
                    case 1:{
                        imv.setImage(new Image("scoring_tokens/scoring.jpg", 70, 70, true, false));
                    }break;
                    case 2:{
                        imv.setImage(new Image("scoring_tokens/scoring_2.jpg", 70, 70, true, false));
                    }break;
                    case 4:{
                        imv.setImage(new Image("scoring_tokens/scoring_4.jpg", 70, 70, true, false));
                    }break;
                    case 6:{
                        imv.setImage(new Image("scoring_tokens/scoring_6.jpg", 70, 70, true, false));
                    }break;
                    case 8:{
                        imv.setImage(new Image("scoring_tokens/scoring_8.jpg", 70, 70, true, false));
                    }break;
                }
                Position freePos = getFirstEmptySpot(TokenContainer);
                if(freePos!=null){
                    TokenContainer.add(imv,freePos.getY(), freePos.getX());
                }

            }
        });
    }

    /**
     * this method is used for updating the view of the tokens of a certain common goal
     * @param n 1 if the common goal to be updated is the first one, 2 if it is the second one
     * @param tokens list of the scoring token to add the view
     */
    public void updateCommonGoalCardTokens(int n, List<ScoringToken> tokens){
        Platform.runLater(()->{
//
            if(tokens==null){
//
                return;
            }
            if(tokens.size()==0){
//
                if(n==1){
                    cgc1tokens.setImage(null);
                }else if(n==2){
                    cgc2tokens.setImage(null);
                }
                return;
            }
            OptionalInt optionalInt = tokens.stream().mapToInt(ScoringToken::getPoints).max();
            int maxAvailablePts = optionalInt.getAsInt();
            if(n==1){
                //cgc1tokens
//
                cgc1tokens.setImage(new Image("scoring_tokens/scoring_"+maxAvailablePts+".jpg", 72, 74,true, false));
            }else if(n==2){
//
                cgc2tokens.setImage(new Image("scoring_tokens/scoring_"+maxAvailablePts+".jpg", 72, 74,true, false));
            }
            else{
//                System.out.println("N = "+n);
            }
        });
    }

    /**
     * This method is udes to set the End Game token to the player that has received it in his scene
     */
    public void setEndGameToken(){
        Platform.runLater(()->{
            Image img = new Image("scoring_tokens/endgame.jpg", 60,60,true, false);
            MyEndGameToken.setImage(img);
        });
    }

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

    /**
     * This method is called to load the EndGameScene when the game has ended.
     * @param leaderboard : the game leaderboard used to show the game results.
     */

    public void switchToEndGameScene(List<Player> leaderboard){
        Platform.runLater(()->{
            Scene scene;
            Parent root;
            ClassLoader classLoader = MainGUI.class.getClassLoader();
            URL fxmlPath = classLoader.getResource("EndGameScene.fxml");
            if(fxmlPath==null){
                throw new IllegalStateException("FXML not found");
            }
            FXMLLoader loader = new FXMLLoader(fxmlPath);
            try {
                root = loader.load();
                scene = new Scene(root);
                EndGameController endGameController = loader.getController();
                Stage stage = (Stage) MyShelf.getScene().getWindow();
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
                endGameController.showLeaderboard(leaderboard);
            } catch (IOException e) {
                System.out.println("cannot load next scene");
            }

        });

    }

    /**
     * This method is used to return to the LogIn scene.This method switches the application's scene to the login scene, allowing the user to log in again.
     * It loads the "LoginScene.fxml" file, sets the necessary controller and client, and displays the new scene in a new stage.
     * Additionally, it handles the close request of the stage by exiting the platform when the close button is pressed.
     */

    public void backToLogin(){
        Platform.runLater(()->{
            try{
                try{
                    System.out.println("hey let's head back to login");
                    Scene scene;
                    Parent root;
                    ClassLoader classLoader = MainGUI.class.getClassLoader();
                    URL fxmlPath = classLoader.getResource("LoginScene.fxml");
                    if(fxmlPath==null){
                        throw new IllegalStateException("FXML not found");
                    }
                    FXMLLoader loader = new FXMLLoader(fxmlPath);
                    root = loader.load();
                    LoginSceneController loginSceneController = loader.getController();
                    loginSceneController.setDisconnected(true);
                    loginSceneController.setClient(this.clientRMI);

                    Stage stage = (Stage) chatButton.getScene().getWindow();
                    stage.setOnCloseRequest(e->{
                        e.consume();
                        Platform.exit();
                    });
                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.show();
                }catch(IOException e){

                }
            }catch(NullPointerException ne){

            }

        });
    }

    /**
     * This method sets up the chat interface.
     * This method configures the chat interface by making the necessary components visible and setting an initial message.
     * It shows the message text area, sets the welcome message, and makes the chat button and text field visible for user interaction.
     * The chat interface allows users to send messages and engage in conversations with others.
     */
    public void chatSetUp (){
        messageTextArea2.setVisible(true);
        messageTextArea2.setText("Welcome to My Shelfie! To chat with others just type in the box below, to chat privately with another player type @NameOfPlayer followed by the message you wish to send");
        messageTextArea2.appendText("\n");
        chatButton.setVisible(true);
        chatTextField.setVisible(true);
    }

    public void close(){
        Platform.runLater(()->{
            Platform.exit();
        });
    }

    /**
     * This method is used to set the first player seat only in the scene of the player who has the first player seat.
     */

    public void setFirstPlayerSeat(){
        firstPlayerSeat.setImage(new Image("misc/firstplayertoken.png",50,50,true,false));
    }
}