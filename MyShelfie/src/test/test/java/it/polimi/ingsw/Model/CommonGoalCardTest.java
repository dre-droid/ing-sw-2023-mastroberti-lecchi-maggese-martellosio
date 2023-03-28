package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.*;
import main.java.it.polimi.ingsw.Model.*;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommonGoalCardTest {

    @Test
    public void hasAlredyBeenRewarded_playerAlredyRewarded_returnTrue(){
        Player player = new Player("Francesco", null, false,null);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        cgc.addPlayerToAlredyBeenRewarded(player);
        assertTrue(cgc.hasAlredyBeenRewarded(player));
    }

    @Test
    public void hasAlredyBeenRewarded_playerNotRewardedYet_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        assertFalse(cgc.hasAlredyBeenRewarded(player));
    }

    @Test
    public void getReward_PlayerHasAllRightsToCollectReward_RewardIsCollected(){
        Player player = new Player("Francesco", null, false,null);
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
        Player player = new Player("Francesco", null, false, null);
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
        Player player = new Player("Francesco", null, false, null);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);

        assertThrows(CannotCollectRewardException.class,()->{
            cgc.getReward(player);
        });
    }

    @Test
    public void isSatisfied_DiagonalStrategy_shelfMatchCommonGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        Diagonal diagonal = new Diagonal();
        CommonGoalCard cgc = new CommonGoalCard(diagonal, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i =0;i<5;i++){
            grid[i][i]= new Tile(Type.CAT);
        }
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_DiagonalStrategy_shelfDoesNotMatchCommonGoal_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        Diagonal diagonal = new Diagonal();
        CommonGoalCard cgc = new CommonGoalCard(diagonal, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i =0;i<2;i++){
            grid[i][i]= new Tile(Type.CAT);
        }
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_eightOfTheSameType_shelfMatchTheGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        EightofSameType eightofSameType = new EightofSameType();
        CommonGoalCard cgc = new CommonGoalCard(eightofSameType, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i =0;i<6;i++){
            for(int j=0;j<5;j++){
                if(i%2==0 && j%2==0)
                    grid[i][j] = new Tile(Type.GAME);
            }
        }
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_eightOfTheSameType_onlySevenOfTheSameType_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        EightofSameType eightofSameType = new EightofSameType();
        CommonGoalCard cgc = new CommonGoalCard(eightofSameType, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i =0;i<6;i++){
            grid[i][0] = new Tile(Type.FRAME);
        }
        grid[3][4]= new Tile(Type.FRAME);
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_FourCornerOfTheSameType_shelfMatchTheGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        FourCornerOfTheSameType fourCornerOfTheSameType = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(fourCornerOfTheSameType, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[0][0] = new Tile(Type.PLANT);
        grid[0][4] = new Tile(Type.PLANT);
        grid[5][0] = new Tile(Type.PLANT);
        grid[5][4] = new Tile(Type.PLANT);
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_FourCornerOfTheSameType_shelfDoesNotMatchTheGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        FourCornerOfTheSameType fourCornerOfTheSameType = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(fourCornerOfTheSameType, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[0][0] = new Tile(Type.PLANT);
        grid[0][4] = new Tile(Type.PLANT);
        grid[5][0] = new Tile(Type.PLANT);
        grid[5][4] = new Tile(Type.FRAME);
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_TwoColumnsOfDifferentTypes_shelfMatchTheGoal_returnTrue(){
        Player player = new Player("Francesco", null,false , null);
        TwoColumnsOfDifferentTypes twoColumnsOfDifferentTypes = new TwoColumnsOfDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(twoColumnsOfDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i = 0;i<6;i++){
            Type t;
            switch(i){
                case 0:t=Type.CAT;break;
                case 1:t=Type.PLANT;break;
                case 2:t=Type.FRAME;break;
                case 3:t=Type.BOOK;break;
                case 4:t=Type.GAME;break;
                default:t=Type.TROPHY;
            }
            grid[i][0] = new Tile(t);
            grid[i][4] = new Tile(t);
        }

        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_TwoColumnsOfDifferentTypes_shelfDoesNotMatchTheGoal_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        TwoColumnsOfDifferentTypes twoColumnsOfDifferentTypes = new TwoColumnsOfDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(twoColumnsOfDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i = 0;i<6;i++){
            Type t;
            switch(i){
                case 0:t=Type.CAT;break;
                case 1:t=Type.PLANT;break;
                case 2:t=Type.PLANT;break;
                case 3:t=Type.BOOK;break;
                case 4:t=Type.PLANT;break;
                default:t=Type.TROPHY;
            }
            grid[i][0] = new Tile(t);
            grid[i][4] = new Tile(t);
        }

        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_XShapedTiles_shelfMatchTheGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        XShapedTiles xShapedTiles = new XShapedTiles();
        CommonGoalCard cgc = new CommonGoalCard(xShapedTiles, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[3][3] = new Tile(Type.FRAME);
        grid[2][2] = new Tile(Type.FRAME);
        grid[4][4] = new Tile(Type.FRAME);
        grid[2][4] = new Tile(Type.FRAME);
        grid[4][2] = new Tile(Type.FRAME);
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_XShapedTiles_shelfDoesNotMatchTheGoal_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        XShapedTiles xShapedTiles = new XShapedTiles();
        CommonGoalCard cgc = new CommonGoalCard(xShapedTiles, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[3][3] = new Tile(Type.FRAME);
        grid[2][2] = new Tile(Type.PLANT);
        grid[4][4] = new Tile(Type.FRAME);
        grid[2][4] = new Tile(Type.FRAME);
        grid[4][2] = new Tile(Type.FRAME);
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_TwoLinesOfDifferentType_shelfMatchTheGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        TwoLinesOfDifferentTypes twoLinesOfDifferentTypes = new TwoLinesOfDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(twoLinesOfDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i =0;i<5;i++){
            Type t;
            switch(i){
                case 0:t=Type.CAT;break;
                case 1:t=Type.PLANT;break;
                case 2:t=Type.FRAME;break;
                case 3:t=Type.BOOK;break;
                default:t=Type.TROPHY;
            }
            grid[1][i] = new Tile(t);
            grid[3][i] = new Tile(t);
        }
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_TwoLinesOfDifferentType_shelfDoesNotMatchTheGoal_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        TwoLinesOfDifferentTypes twoLinesOfDifferentTypes = new TwoLinesOfDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(twoLinesOfDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int i =0;i<5;i++){
            Type t;
            switch(i){
                case 0:t=Type.CAT;break;
                case 1:t=Type.PLANT;break;
                case 2:t=Type.PLANT;break;
                default:t=Type.TROPHY;
            }
            grid[1][i] = new Tile(t);
            grid[3][i] = new Tile(t);
        }
        assertFalse(cgc.isSatisfiedBy(player));
    }
}