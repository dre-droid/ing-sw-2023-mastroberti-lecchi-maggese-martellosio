package main.java.it.polimi.ingsw.GUI;

import javafx.beans.property.SimpleStringProperty;

public class TableRecord {
    private final SimpleStringProperty player;
    private final SimpleStringProperty score;



    public TableRecord(String playerName, String score) {
        this.player = new SimpleStringProperty(playerName);
        this.score = new SimpleStringProperty(score);
    }

    public String getPlayer(){
        return player.get();
    }

    public String getScore(){
        return score.get();
    }

    public void setPlayer(String name){
        this.player.set(name);
    }

    public void setScore(String score){
        this.score.set(score);
    }
}