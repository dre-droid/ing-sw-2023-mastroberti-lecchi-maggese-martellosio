package main.java.it.polimi.ingsw.Model;

import java.io.Serializable;

public class ScoringToken implements Serializable {

    int points;
    public ScoringToken(int points){
        this.points= points;
    }

    public int getPoints(){
        return this.points;
    }

}
