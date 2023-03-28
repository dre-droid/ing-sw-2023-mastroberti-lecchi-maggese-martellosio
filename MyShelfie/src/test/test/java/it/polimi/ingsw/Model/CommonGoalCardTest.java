package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.*;
import main.java.it.polimi.ingsw.Model.*;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommonGoalCardTest {

    @Test
    public void hasAlredyBeenRewarded_playerAlredyRewarded_returnTrue(){
        Player player = new Player("Francesco", null, false);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        cgc.addPlayerToAlredyBeenRewarded(player);
        assertTrue(cgc.hasAlredyBeenRewarded(player));
    }

    @Test
    public void hasAlredyBeenRewarded_playerNotRewardedYet_returnFalse(){
        Player player = new Player("Francesco", null, false);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        assertFalse(cgc.hasAlredyBeenRewarded(player));
    }

    @Test
    public void getReward_PlayerHasAllRightsToCollectReward_RewardIsCollected(){
        Player player = new Player("Francesco", null, false);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        cgc.addPlayerToWhoCompleted(player);
        try{
            assertNotNull(cgc.getReward(player));
        }catch(CannotCollectRewardException e){
            e.printStackTrace();
        }

    }

    @Test
    public void getReward_PlayerHasAlredyCollectedThereward_CannotCollectRewardExceptionIsThrown(){
        Player player = new Player("Francesco", null, false);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        cgc.addPlayerToWhoCompleted(player);
        cgc.addPlayerToAlredyBeenRewarded(player);
        assertThrows(CannotCollectRewardException.class,()->{
            cgc.getReward(player);
        });

    }

    @Test
    public void getReward_PlayerHasNotCompletedTheGoalYet_CannotCollectRewardExceptionIsThrown(){
        Player player = new Player("Francesco", null, false);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);

        assertThrows(CannotCollectRewardException.class,()->{
            cgc.getReward(player);
        });
    }

    @Test
    public void isSatisfied_DiagonalStrategy_shelfMatchCommonGoal_returnTrue(){
        Player player = new Player("Francesco", null, false);
        Diagonal diagonal = new Diagonal();
        CommonGoalCard cgc = new CommonGoalCard(diagonal, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i =0;i<5;i++){
            grid[i][i]= new Tile(Type.CAT);
        }
        assertTrue(cgc.isSatisfiedBy(player));
    }





}