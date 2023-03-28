package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.Player;
import main.java.it.polimi.ingsw.Model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francesco Martellosio
 * This class represent the common goals that the player must fulfill in order to gain points
 */
public class CommonGoalCard {
    StrategyCommonGoal strategy;
    List<ScoringToken> tokens;
    List<String> playersWhoCompleted;
    List<String> playersAlredyRewarded;

    public CommonGoalCard(StrategyCommonGoal strategy,int numOfPlayers){
        this.strategy = strategy;
        this.tokens = new ArrayList<ScoringToken>();
        ScoringToken token8pts = new ScoringToken(8);
        ScoringToken token4pts = new ScoringToken(4);
        switch (numOfPlayers){
            case 2: {
                tokens.add(token4pts);
                tokens.add(token8pts);
            }break;
            case 3:{
                ScoringToken token6pts = new ScoringToken(6);
                tokens.add(token4pts);
                tokens.add(token6pts);
                tokens.add(token8pts);
            }
            case 4:{
                ScoringToken token6pts = new ScoringToken(6);
                ScoringToken token2pts = new ScoringToken(2);
                tokens.add(token2pts);
                tokens.add(token4pts);
                tokens.add(token6pts);
                tokens.add(token8pts);
            }
        }
        this.playersWhoCompleted = new ArrayList<String>();
        this.playersAlredyRewarded = new ArrayList<String>();
    }

    /**
     * @author Francesco Martellosio
     * @param player it's the player who is checking if he has completed the common goal
     * @return true if the player has completed the common goal, false otherwise
     */
    public boolean isSatisfiedBy(Player player){
        boolean outcome = strategy.executeStrategy(player.getShelf());
        if(outcome){
            playersWhoCompleted.add(player.getNickname());
        }
        return outcome;
    }

    /**
     * @author Francesco Martellosio
     * This method checks if the player passed in the parameter has alredy collected the reward from this common goal
     * @param player it's one of the players
     * @return true if the player has already took the reward from completing this v
     */
    public boolean hasAlredyBeenRewarded(Player player){
        for(String nickname: this.playersAlredyRewarded){
            if(player.getNickname().equals(nickname))
                return true;
        }
        return false;
    }

    /**
     * This method check if the player has alredy completed in a previous turn this common goal
     * @param player it's one of the players
     * @return true if the player alredy completed the common goal, false otherwise
     */
    private boolean hasCompletedThisGoal(Player player){
        for(String nickname: this.playersWhoCompleted){
            if(player.getNickname().equals(nickname))
                return true;
        }
        return false;
    }


    /**
     * This method give to the player his scoring token as a reward for completing the goal
     * @param player it's one of the players
     * @return the scoringToken with the max points available at the moment if the player has completed the goal and hasn't alredy
     * collected the reward
     * @throws CannotCollectRewardException if the player has alredy collected the reward or if he hasn't completed the goal yet
     */
    public ScoringToken getReward(Player player) throws CannotCollectRewardException{
        if(hasCompletedThisGoal(player) && !hasAlredyBeenRewarded(player)){
            ScoringToken collectedToken = tokens.remove(tokens.size()-1);
            playersAlredyRewarded.add(player.getNickname());
            return collectedToken;
        }
        else if(!hasCompletedThisGoal(player)){
            throw new CannotCollectRewardException("You have not completed this common goal yet");
        }
        else{
            throw new CannotCollectRewardException("You have alredy collected the reward from this common goal");
        }
    }


}
