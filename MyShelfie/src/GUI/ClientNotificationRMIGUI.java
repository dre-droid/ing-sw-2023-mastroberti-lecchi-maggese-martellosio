package GUI;

import Server.RMI.ClientNotificationInterfaceRMI;
import Server.RMI.ClientNotificationRMI;
import Server.RMI.ClientRMI;
import Server.RMI.RMIinterface;
import main.java.it.polimi.ingsw.Model.*;
import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.CommonGoalCard;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ClientNotificationRMIGUI extends java.rmi.server.UnicastRemoteObject implements ClientNotificationInterfaceRMI {
    private RMIinterface serverRMI;

    GameSceneController gsc;

    private int port;

    private String nickname;
    private boolean MyTurnFlag;
    private boolean EndGameFlag;
    private boolean GameStartFlag;

    private List<Tile> drawnTiles;
    public int  joinGameOutcome = -5;

    private String myIp;

    private List<CommonGoalCard> commonGoalCards;

    public ClientNotificationRMIGUI(String serverIp) throws RemoteException{
        try{
            Registry registryServer = LocateRegistry.getRegistry(serverIp);
            serverRMI = (RMIinterface) registryServer.lookup("MyShelfie");
        }catch(RemoteException | NotBoundException e){
            System.out.println("ClientNotificationRMIGUI--> ERROR: cannot connect to server");
        }
        //ip address
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            myIp = inetAddress.getHostAddress();
            //System.out.println("my ip is = "+myIp);
        } catch (UnknownHostException e) {
            System.out.println("cannot get ip address ");
        }
        MyTurnFlag = false;
        EndGameFlag = false;
        GameStartFlag = false;
    }


    public List<Player> getLeaderboard(){
        try{
            return serverRMI.getLeaderboard();
        }catch(RemoteException remoteException){
            throw new RuntimeException(remoteException);
        }

    }

    public void startNotificationServer() throws RemoteException{
        Random random = new Random();
        port = random.nextInt(3000,6000);
        Registry clientRegistry = LocateRegistry.createRegistry(port);
        clientRegistry.rebind("Client",this);
    }

    public void setnickname(String nick){
        this.nickname = nick;
    }

    public String getNickname(){return this.nickname;}

    public int joinGame() throws RemoteException{
        System.out.println("trying login");
        return serverRMI.joinGame(nickname, port, myIp);
    }
    public int joinLobby() throws RemoteException{
        return serverRMI.joinLobby(nickname, port, myIp);
    }
    public void heartbeat(String playerNickname) {
        new Thread(() -> {
            while (true) {
                try {
                    serverRMI.setLastPing(playerNickname);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public boolean createNewGame(int numOfPlayers) throws RemoteException{
        System.out.println("creating new game...");
        return serverRMI.createNewGame(nickname, numOfPlayers, port);
    }



    @Override
    public void gameJoinedCorrectlyNotification() throws RemoteException {

    }

    @Override
    public void problemInJoiningGame(String problem) throws RemoteException {

    }

    @Override
    public void gameCreatedCorrectly() throws RemoteException {

    }

    @Override
    public void cannotCreateNewGame(String problem) throws RemoteException {

    }

    @Override
    public void someoneJoinedTheGame(String nickname) throws RemoteException {

    }
    public void gameHasBeenCreated() throws RemoteException{

    }

    @Override
    public void startingTheGame(String startingPlayer) throws RemoteException {
        GameStartFlag=true;
        System.out.println("GAME HAS STARTED LETSGOOOOOOO");
        updateGUIAtBeginningOfGame();
    }

    public void updateGUIAtBeginningOfGame(){
        try{
            TilePlacingSpot[][] board= serverRMI.getBoard();
            Map<Integer, PersonalGoalCard> pgcMap = serverRMI.getPGCmap();
            PersonalGoalCard pgc = serverRMI.getPGC(nickname);
            commonGoalCards = serverRMI.getCommonGoalCards();
            List<Player> leaderboard = serverRMI.getLeaderboard();
            String isPlaying = serverRMI.getIsPlaying();
            if(gsc!=null) {
                System.out.println("gsc non è null per "+nickname);
                gsc.updateGUIAtBeginningOfGame(board, pgcMap, pgc, commonGoalCards, leaderboard, isPlaying);
                if(serverRMI.isMyTurn(nickname)){
                    gsc.setFirstPlayerSeat();
                }
            }
            else{
                System.out.println("gsc null ma non si sa perchè");
            }
        }catch(RemoteException re){
            System.out.println("Problem in the update of the gui at the beginning of the game");
            re.printStackTrace();
        }
    }

    public boolean drawTilesFromBoard(int x, int y, int amount, Board.Direction direction){
        try {
            drawnTiles = serverRMI.drawTilesFromBoard(nickname, x, y, amount, direction);
            return  drawnTiles!=null;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void rearrangeDrawnTiles(int pos1, int pos2){
        try{
            Tile temp = drawnTiles.get(pos1);
            drawnTiles.set(pos1, drawnTiles.get(pos2));
            drawnTiles.set(pos2, temp);
        }catch (IndexOutOfBoundsException e){
            return;
        }

    }

    public boolean insertTilesInShelf(int column ) {
        try {
            boolean returnValue = serverRMI.insertTilesInShelf(nickname,drawnTiles,column);
            if(returnValue){
                drawnTiles = null;
                MyTurnFlag = false;
            }
            return returnValue;

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public Tile[][] getMyShelf(){
        try {
            return serverRMI.getMyShelf(nickname);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public Tile[][] getShelfOfPlayer(String player){
        try {
            return serverRMI.getMyShelf(player);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void someoneHasCompletedACommonGoal(String playerNickname, CommonGoalCard cgc) throws RemoteException {
        if(playerNickname.equals(this.nickname)) {
            List<ScoringToken> tokens = serverRMI.getMyTokens(this.nickname);
            //update the view
            this.gsc.updateScoringTokens(tokens);
        }
        int nOfCommonGoalCard=-1;
        for(CommonGoalCard c: commonGoalCards){
            if(cgc.getDescription().equals(c.getDescription())){
                nOfCommonGoalCard = commonGoalCards.indexOf(c);
            }
        }
        System.out.println("common goal number: "+nOfCommonGoalCard);
        if(nOfCommonGoalCard==0){
            this.gsc.updateCommonGoalCardTokens(1,serverRMI.getCgcTokens(cgc));
        }else if (nOfCommonGoalCard==1){
            this.gsc.updateCommonGoalCardTokens(2,serverRMI.getCgcTokens(cgc));
        }
        else{
            System.out.println("errore -1");
        }
    }

    @Override
    public void aTurnHasEnded(String currentPlayerNickname, String nextPlayerNickname) throws RemoteException {
        gsc.updateTurnLabel(nextPlayerNickname);
        gsc.updateLeaderboard(serverRMI.getLeaderboard());
        gsc.updateBoard(serverRMI.getBoard());
    }

    @Override
    public void gameIsOver(List<String> leaderboard) throws RemoteException {
        //popup
        gsc.endGamePopup();
    }

    @Override
    public void moveIsNotValid() throws RemoteException {

    }

    @Override
    public void announceCommonGoals(String commonGoals) throws RemoteException {

    }

    @Override
    public void runOutOfTime() throws RemoteException {

    }

    @Override
    public void ping() throws RemoteException {

    }

    @Override
    public void startTurn() throws RemoteException {
        MyTurnFlag = true;
    }

    @Override
    public void receiveMessage(String text, String sender, Boolean pm) throws RemoteException {
        if(pm)
            gsc.rmiMessageTextArea("[MESSAGE FROM " + sender + " TO YOU]: " + text);
        else
            gsc.rmiMessageTextArea("[MESSAGE FROM "+sender+"]: "+text);
    }

    public void sendChatMessage(String message) throws RemoteException{
        String text = "", receiver = "";
        int atIndex;
        if(message.startsWith("@")){
            receiver = message.substring( 1);
            atIndex = receiver.indexOf(' ');
            text = receiver.substring(atIndex + 1);
            receiver = receiver.substring(0, atIndex);
            if(!Objects.equals(receiver, nickname))
                serverRMI.chatMessage(nickname, text, receiver, true);
        }
        else {
            receiver = "all";
            serverRMI.chatMessage(nickname, message, receiver, false);

        }
    }

    public void quitGame(){
        try {
            serverRMI.quitGame(nickname);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void invalidCommandSent() throws RemoteException {

    }

    @Override
    public void updateBoard(TilePlacingSpot[][] boardView) throws RemoteException {
        System.out.println(nickname+" has updated the board");
        gsc.updateBoard(boardView);
    }

    @Override
    public void updateOppShelf(String nickname, Tile[][] grid) throws RemoteException {
        gsc.updateOppShelf(nickname,grid);
    }

    public void setGameSceneController(GameSceneController controller){
        System.out.println("controller set");
        gsc = controller;
        if(gsc!=null){
            System.out.println("gsc non è nullo nel set");
        }
    }

    public TilePlacingSpot[][] getBoard(){
        try {
            return serverRMI.getBoard();
        } catch (RemoteException e) {
            System.out.println("problem in getting the board");
            return null;
        }

    }

    public boolean hasGameStarted(){
        try {
            return serverRMI.hasGameStarted();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isMyTurn(){
        return MyTurnFlag;
    }
    public void nickNameAlreadyInUse() throws RemoteException{

    }
    public void notifyOfDisconnection() throws RemoteException{

    }
    public void joinGameOutcome(int outcome) throws RemoteException{
        joinGameOutcome = outcome;
        synchronized (this) {
            this.notifyAll();
        }
    }
    public boolean isGameBeingCreated() throws RemoteException{
        return serverRMI.isGameBeingCreated();
    }
    public boolean firstInLobby (String nickname) throws RemoteException{
        return serverRMI.firstInLobby(nickname);
    }
}
