package GUI;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import main.java.it.polimi.ingsw.Model.Player;
import java.util.List;

public class EndGameController {
    @FXML
    private GridPane gridLeaderboard;

    public void showLeaderboard(List<Player> leaderboard){
        Image img = null;
        ImageView imv;
        for(int i=0;i<leaderboard.size();i++){
            switch (i){
                case 0: img = new Image("game_stuff/gold-winner-trophy-icon.png",120,120,true, false);break;
                case 1: img = new Image("game_stuff/number-two-round-icon.png",120,120,true, false);break;
                case 2: img = new Image("game_stuff/number-three-round-icon.png",120,120,true, false);break;
                case 3: img = new Image("game_stuff/number-four-round-icon.png",120,120,true, false);break;
            }
            imv = new ImageView(img);
            gridLeaderboard.add(imv, 0,i);

        }
    }
}
