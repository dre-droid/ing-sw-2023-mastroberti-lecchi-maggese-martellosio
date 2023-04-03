package test.java.it.polimi.ingsw.Model;

import main.java.it.polimi.ingsw.Model.CommonGoalCardStuff.*;
import main.java.it.polimi.ingsw.Model.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

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
    public void getReward_FirstPlayerToCompleteTheGoal_8ptsTokenReturned(){
        Player player = new Player("Francesco", null, false, null);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        cgc.addPlayerToWhoCompleted(player);
        try{
            ScoringToken st = cgc.getReward(player);
            assertEquals(8, st.getPoints());
        }catch(CannotCollectRewardException c){
            c.printStackTrace();
        }
    }

    @Test
    public void getReward_SecondPlayerToCompleteTheGoal_8ptsTokenReturned(){
        Player player1 = new Player("Francesco", null, false, null);
        Player player2 = new Player("Marco", null, false, null);
        FourCornerOfTheSameType strategy = new FourCornerOfTheSameType();
        CommonGoalCard cgc = new CommonGoalCard(strategy,3);
        cgc.addPlayerToWhoCompleted(player1);
        cgc.addPlayerToWhoCompleted(player2);

        try{
            ScoringToken st1 = cgc.getReward(player1);
            ScoringToken st2 = cgc.getReward(player2);
            assertEquals(6, st2.getPoints());
        }catch(CannotCollectRewardException c){
            c.printStackTrace();
        }
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

    @Test
    public void isSatisfied_IncreasingOrDecreasingHeight_DecreasingColumnsStartingFrom5Height_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        IncreasingOrDecreasingHeight increasingOrDecreasingHeight = new IncreasingOrDecreasingHeight();
        CommonGoalCard cgc = new CommonGoalCard(increasingOrDecreasingHeight, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int j = 0;j<5;j++){
            for(int i=0;i<5-j;i++){
                grid[i][j]= new Tile(Type.CAT);
            }
        }
        assertTrue(cgc.isSatisfiedBy(player));

    }

    @Test
    public void isSatisfied_IncreasingOrDecreasingHeight_DecreasingColumnsStartingFrom6Height_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        IncreasingOrDecreasingHeight increasingOrDecreasingHeight = new IncreasingOrDecreasingHeight();
        CommonGoalCard cgc = new CommonGoalCard(increasingOrDecreasingHeight, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int j = 0;j<5;j++){
            for(int i=0;i<6-j;i++){
                grid[i][j]= new Tile(Type.CAT);
            }
        }
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_IncreasingOrDecreasingHeight_IncreasingColumnsStartingFrom6Height_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        IncreasingOrDecreasingHeight increasingOrDecreasingHeight = new IncreasingOrDecreasingHeight();
        CommonGoalCard cgc = new CommonGoalCard(increasingOrDecreasingHeight, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int j = 0;j<5;j++){
            for(int i=0;i<6-4+j;i++){
                grid[i][j]= new Tile(Type.CAT);
            }
        }
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_IncreasingOrDecreasingHeight_IncreasingColumnsStartingFrom5Height_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        IncreasingOrDecreasingHeight increasingOrDecreasingHeight = new IncreasingOrDecreasingHeight();
        CommonGoalCard cgc = new CommonGoalCard(increasingOrDecreasingHeight, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int j = 0;j<5;j++){
            for(int i=0;i<6-5+j;i++){
                grid[i][j]= new Tile(Type.CAT);
            }
        }
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_IncreasingOrDecreasingHeight_DecreasingHeightsButWithHoles_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        IncreasingOrDecreasingHeight increasingOrDecreasingHeight = new IncreasingOrDecreasingHeight();
        CommonGoalCard cgc = new CommonGoalCard(increasingOrDecreasingHeight, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int j = 0;j<5;j++){
            for(int i=0;i<6-j;i++){
                grid[i][j]= new Tile(Type.CAT);
            }
        }
        grid[0][0]=null;
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_IncreasingOrDecreasingHeight_IncreasingHeightsButWithHoles_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        IncreasingOrDecreasingHeight increasingOrDecreasingHeight = new IncreasingOrDecreasingHeight();
        CommonGoalCard cgc = new CommonGoalCard(increasingOrDecreasingHeight, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int j = 0;j<5;j++){
            for(int i=0;i<6-4+j;i++){
                grid[i][j]= new Tile(Type.CAT);
            }
        }
        grid[1][3]=null;
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_SquaredShapedGroups_shelfMatchGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        SquaredShapedGroups squaredShapedGroups = new SquaredShapedGroups();
        CommonGoalCard cgc = new CommonGoalCard(squaredShapedGroups, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[1][1] = new Tile(Type.TROPHY);
        grid[1][2] = new Tile(Type.TROPHY);
        grid[2][1] = new Tile(Type.TROPHY);
        grid[2][2] = new Tile(Type.TROPHY);

        grid[4][2] = new Tile(Type.TROPHY);
        grid[4][3] = new Tile(Type.TROPHY);
        grid[5][2] = new Tile(Type.TROPHY);
        grid[5][3] = new Tile(Type.TROPHY);
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_SquaredShapedGroups_squaresOfDifferentTypes_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        SquaredShapedGroups squaredShapedGroups = new SquaredShapedGroups();
        CommonGoalCard cgc = new CommonGoalCard(squaredShapedGroups, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[1][1] = new Tile(Type.TROPHY);
        grid[1][2] = new Tile(Type.TROPHY);
        grid[2][1] = new Tile(Type.TROPHY);
        grid[2][2] = new Tile(Type.TROPHY);

        grid[4][2] = new Tile(Type.CAT);
        grid[4][3] = new Tile(Type.CAT);
        grid[5][2] = new Tile(Type.CAT);
        grid[5][3] = new Tile(Type.CAT);
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_SquaredShapedGroups_squaresOverlap_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        SquaredShapedGroups squaredShapedGroups = new SquaredShapedGroups();
        CommonGoalCard cgc = new CommonGoalCard(squaredShapedGroups, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[1][1] = new Tile(Type.TROPHY);
        grid[1][2] = new Tile(Type.TROPHY);
        grid[2][1] = new Tile(Type.TROPHY);
        grid[2][2] = new Tile(Type.TROPHY);

        grid[2][2] = new Tile(Type.TROPHY);
        grid[2][3] = new Tile(Type.TROPHY);
        grid[3][2] = new Tile(Type.TROPHY);
        grid[3][3] = new Tile(Type.TROPHY);
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_SixGroupsOfAtLeastTwoSameTypeTiles_shelfMatchGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        SixGroupsOfAtLeastTwoSameTypeTiles sixGroupsOfAtLeastTwoSameTypeTiles = new SixGroupsOfAtLeastTwoSameTypeTiles();
        CommonGoalCard cgc = new CommonGoalCard(sixGroupsOfAtLeastTwoSameTypeTiles, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[0][0] = new Tile(Type.TROPHY);
        grid[1][0] = new Tile(Type.TROPHY);
        grid[1][1] = new Tile(Type.TROPHY);
        grid[2][1] = new Tile(Type.TROPHY);
        grid[2][2] = new Tile(Type.TROPHY);
        grid[2][3] = new Tile(Type.TROPHY);

        grid[2][0] = new Tile(Type.CAT);
        grid[3][0] = new Tile(Type.CAT);

        grid[4][0] = new Tile(Type.FRAME);
        grid[5][0] = new Tile(Type.FRAME);
        grid[4][1] = new Tile(Type.FRAME);
        grid[5][1] = new Tile(Type.FRAME);

        grid[3][2] = new Tile(Type.GAME);
        grid[3][3] = new Tile(Type.GAME);
        grid[4][2] = new Tile(Type.GAME);
        grid[4][3] = new Tile(Type.GAME);
        grid[5][2] = new Tile(Type.GAME);
        grid[5][3] = new Tile(Type.GAME);

        grid[4][4] = new Tile(Type.FRAME);
        grid[5][4] = new Tile(Type.FRAME);

        grid[0][3] = new Tile(Type.PLANT);
        grid[0][4] = new Tile(Type.PLANT);
        grid[1][4] = new Tile(Type.PLANT);
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_SixGroupsOfAtLeastTwoSameTypeTiles_shelfDoesNotMatchGoal_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        SixGroupsOfAtLeastTwoSameTypeTiles sixGroupsOfAtLeastTwoSameTypeTiles = new SixGroupsOfAtLeastTwoSameTypeTiles();
        CommonGoalCard cgc = new CommonGoalCard(sixGroupsOfAtLeastTwoSameTypeTiles, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        grid[0][0] = new Tile(Type.TROPHY);
        grid[1][0] = new Tile(Type.TROPHY);
        grid[1][1] = new Tile(Type.TROPHY);
        grid[2][1] = new Tile(Type.TROPHY);
        grid[2][2] = new Tile(Type.TROPHY);
        grid[2][3] = new Tile(Type.TROPHY);

        grid[2][0] = new Tile(Type.CAT);
        grid[3][0] = new Tile(Type.CAT);

        grid[4][0] = new Tile(Type.FRAME);
        grid[5][0] = new Tile(Type.FRAME);
        grid[4][1] = new Tile(Type.FRAME);
        grid[5][1] = new Tile(Type.FRAME);
        grid[5][2] = new Tile(Type.FRAME);
        grid[5][3] = new Tile(Type.FRAME);

        grid[3][2] = new Tile(Type.GAME);
        grid[3][3] = new Tile(Type.GAME);
        grid[4][2] = new Tile(Type.GAME);
        grid[4][3] = new Tile(Type.GAME);

        grid[4][4] = new Tile(Type.FRAME);
        grid[5][4] = new Tile(Type.FRAME);

        grid[0][3] = new Tile(Type.PLANT);
        grid[0][4] = new Tile(Type.PLANT);
        grid[1][4] = new Tile(Type.PLANT);
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_FourGroupsOfAtLeastFourSameTypeTiles_shelfMatchGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        FourGroupsOfAtLeastFourSameTypeTiles fourGroupsOfAtLeastFourSameTypeTiles = new FourGroupsOfAtLeastFourSameTypeTiles();
        CommonGoalCard cgc = new CommonGoalCard(fourGroupsOfAtLeastFourSameTypeTiles, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();

        for(int i=0;i<2;i++)
            for(int j=0;j<2;j++)
                grid[i][j]=new Tile(Type.CAT);

        for(int i=0;i<2;i++)
            for(int j=2;j<4;j++)
                grid[i][j]=new Tile(Type.TROPHY);
        for(int j=0;j<4;j++)
            grid[2][j] = new Tile(Type.TROPHY);

        for(int i=3;i<6;i++)
            grid[i][0] = new Tile(Type.PLANT);
        grid[3][1] = new Tile(Type.PLANT);

        for(int i=3;i<6;i++)
            for(int j=2;j<4;j++)
                grid[i][j] = new Tile(Type.GAME);
        for(int i=4;i<6;i++)
            grid[i][1] = new Tile(Type.GAME);

        assertTrue(cgc.isSatisfiedBy(player));

    }

    @Test
    public void isSatisfied_FourGroupsOfAtLeastFourSameTypeTiles_shelfDoesNotMatchGoal_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        FourGroupsOfAtLeastFourSameTypeTiles fourGroupsOfAtLeastFourSameTypeTiles = new FourGroupsOfAtLeastFourSameTypeTiles();
        CommonGoalCard cgc = new CommonGoalCard(fourGroupsOfAtLeastFourSameTypeTiles, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();

        for(int i=0;i<6;i++)
            for(int j=0;j<3;j++)
                grid[i][j]=new Tile(Type.CAT);
        for(int i=0;i<6;i++)
            for(int j=3;j<5;j++)
                grid[i][j]=new Tile(Type.PLANT);


        assertFalse(cgc.isSatisfiedBy(player));

    }

    @Test
    public void isSatisfied_FourRowsOfMaxThreeDifferentTypes_ShelfMatchGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        FourRowsOfMaxThreeDifferentTypes fourRowsOfMaxThreeDifferentTypes = new FourRowsOfMaxThreeDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(fourRowsOfMaxThreeDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int row=0;row<4;row++)
            for(int col=0;col<5;col++){
                if(col<2){
                    grid[row][col] = new Tile(Type.CAT);
                }
                else{
                    if(col==2)
                        grid[row][col] = new Tile(Type.PLANT);
                    else
                        grid[row][col] = new Tile(Type.TROPHY);
                }
            }
        assertTrue(cgc.isSatisfiedBy(player));
    }


    @Test
    public void isSatisfied_FourRowsOfMaxThreeDifferentTypes_OnlyThreeRowsAreFull_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        FourRowsOfMaxThreeDifferentTypes fourRowsOfMaxThreeDifferentTypes = new FourRowsOfMaxThreeDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(fourRowsOfMaxThreeDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int row=0;row<3;row++)
            for(int col=0;col<5;col++){
                if(col<2){
                    grid[row][col] = new Tile(Type.CAT);
                }
                else{
                    if(col==2)
                        grid[row][col] = new Tile(Type.PLANT);
                    else
                        grid[row][col] = new Tile(Type.TROPHY);
                }
            }
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_FourRowsOfMaxThreeDifferentTypes_ThreeRowsOkOneWith4DifferentTypes_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        FourRowsOfMaxThreeDifferentTypes fourRowsOfMaxThreeDifferentTypes = new FourRowsOfMaxThreeDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(fourRowsOfMaxThreeDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int row=0;row<4;row++)
            for(int col=0;col<5;col++){
                if(col<2){
                    grid[row][col] = new Tile(Type.CAT);
                }
                else{
                    if(col==2)
                        grid[row][col] = new Tile(Type.PLANT);
                    else
                        grid[row][col] = new Tile(Type.TROPHY);
                }
            }
        grid[3][0] = new Tile(Type.GAME);

        assertFalse(cgc.isSatisfiedBy(player));
    }


    @Test
    public void isSatisfied_ThreeColumnsOfMaxThreeDifferentTypes_ShelfMatchGoal_returnTrue(){
        Player player = new Player("Francesco", null, false, null);
        ThreeColumnsOfMaxThreeDifferentTypes threeColumnsOfMaxThreeDifferentTypes = new ThreeColumnsOfMaxThreeDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(threeColumnsOfMaxThreeDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int row=0;row<6;row++)
            for(int col=0;col<3;col++){
                if(row<2){
                    grid[row][col] = new Tile(Type.CAT);
                }
                else{
                    if(row==2 || row ==5)
                        grid[row][col] = new Tile(Type.PLANT);
                    else
                        grid[row][col] = new Tile(Type.TROPHY);
                }
            }
        assertTrue(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_ThreeColumnsOfMaxThreeDifferentTypes_OnlyTwoColumnsAreFull_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        ThreeColumnsOfMaxThreeDifferentTypes threeColumnsOfMaxThreeDifferentTypes = new ThreeColumnsOfMaxThreeDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(threeColumnsOfMaxThreeDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int row=0;row<6;row++)
            for(int col=0;col<2;col++){
                if(row<2){
                    grid[row][col] = new Tile(Type.CAT);
                }
                else{
                    if(row==2 || row ==5)
                        grid[row][col] = new Tile(Type.PLANT);
                    else
                        grid[row][col] = new Tile(Type.TROPHY);
                }
            }
        assertFalse(cgc.isSatisfiedBy(player));
    }

    @Test
    public void isSatisfied_ThreeColumnsOfMaxThreeDifferentTypes_TwoColumnsOkOneWith4DifferentTypes_returnFalse(){
        Player player = new Player("Francesco", null, false, null);
        ThreeColumnsOfMaxThreeDifferentTypes threeColumnsOfMaxThreeDifferentTypes = new ThreeColumnsOfMaxThreeDifferentTypes();
        CommonGoalCard cgc = new CommonGoalCard(threeColumnsOfMaxThreeDifferentTypes, 3);
        Shelf shelf = player.getShelf();
        Tile[][] grid = shelf.getGrid();
        for(int row=0;row<6;row++)
            for(int col=0;col<3;col++){
                if(row<2){
                    grid[row][col] = new Tile(Type.CAT);
                }
                else{
                    if(row==2 || row ==5)
                        grid[row][col] = new Tile(Type.PLANT);
                    else
                        grid[row][col] = new Tile(Type.TROPHY);
                }
            }
        grid[0][0] = new Tile(Type.GAME);
        assertFalse(cgc.isSatisfiedBy(player));
    }
}