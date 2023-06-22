package GUI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.java.it.polimi.ingsw.Model.Player;

import java.util.List;

public class EndGameController {
    @FXML
    private GridPane gridLeaderboard;

    @FXML
    private Text winnerLabel;

    public void showLeaderboard(List<Player> leaderboard){
        Image img = null;
        ImageView imv;
        Text nickname, points;
        for(int i=0;i<leaderboard.size();i++){
            switch (i){
                case 0: img = new Image("game_stuff/gold-winner-trophy-icon.png", 50, 50, true, false);break;
                case 1: img = new Image("game_stuff/number-two-round-icon.png",50,50,true, false);break;
                case 2: img = new Image("game_stuff/number-three-round-icon.png",50,50,true, false);break;
                case 3: img = new Image("game_stuff/number-four-round-icon.png",50,50,true, false);break;
            }
            imv = new ImageView(img);
            gridLeaderboard.add(imv, 0,i);
            GridPane.setHalignment(imv, Pos.CENTER.getHpos());
            GridPane.setValignment(imv, Pos.CENTER.getVpos());
            Font font = new Font("Garamond",25);
            nickname = new Text();
            nickname.setText(leaderboard.get(i).getNickname());
            nickname.setFont(font);
            points = new Text();
            points.setText(leaderboard.get(i).getScore()+" pts");
            points.setFont(font);
            gridLeaderboard.add(nickname,1,i);
            gridLeaderboard.add(points,2,i);
            GridPane.setHalignment(nickname, Pos.CENTER.getHpos());
            GridPane.setValignment(nickname, Pos.CENTER.getVpos());
            GridPane.setHalignment(points, Pos.CENTER.getHpos());
            GridPane.setValignment(points, Pos.CENTER.getVpos());
        }
        winnerLabel.setText("The winner is "+leaderboard.get(0).getNickname());
        Stage st = (Stage) winnerLabel.getScene().getWindow();
        st.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e->{
            e.consume();
            Platform.exit();
            System.exit(0);
        });
    }
}
