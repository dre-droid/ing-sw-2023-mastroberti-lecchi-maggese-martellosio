package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

public class CannotCollectRewardException extends Exception{
    public CannotCollectRewardException(String errorMessage){
        super(errorMessage);
    }
}
