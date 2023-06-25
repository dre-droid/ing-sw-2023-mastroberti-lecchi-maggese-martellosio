package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;
import main.java.it.polimi.ingsw.Model.Shelf;
import main.java.it.polimi.ingsw.Model.Tile;
import main.java.it.polimi.ingsw.Model.*;

import java.io.Serializable;

public class SquaredShapedGroups implements StrategyCommonGoal, Serializable {
    /*@Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();

        int sqx;
        int sqy;

        for(int col=0;col<4;col++){
            for(int raw=0;raw<5;raw++){
                if(shelfGrid[raw][col] != null && shelfGrid[raw+1][col+1] != null && shelfGrid[raw][col+1] != null && shelfGrid[raw+1][col] != null){
                    if(shelfGrid[raw][col].getType() == shelfGrid[raw+1][col+1].getType() && shelfGrid[raw][col].getType() == shelfGrid[raw][col+1].getType() && shelfGrid[raw][col].getType() == shelfGrid[raw+1][col].getType()){
                        sqx=raw;
                        sqy=col;
                        System.out.println(sqx);
                        System.out.println(sqy);

                        for(int y=sqy;y<4;y++){
                            for(int x=sqx;x<5;x++){
                                if(shelfGrid[x][y] != null && shelfGrid[x+1][y+1] != null && shelfGrid[x][y+1] != null && shelfGrid[x+1][y] != null){
                                    if(shelfGrid[x][y].getType() == shelfGrid[x+1][y+1].getType()  && shelfGrid[x][y].getType() == shelfGrid[x+1][y].getType()  && shelfGrid[x][y+1].getType() == shelfGrid[x][y].getType()){
                                        System.out.println("Qua ci entri bastardo?");
                                        System.out.println(sqx);
                                        System.out.println(sqy);
                                        System.out.println(x);
                                        System.out.println(y);
                                        if(!( x== sqx && y == sqy) && !(x==sqx && y == sqy +1) && !( x == sqx + 1 && y == sqy) && !( x== sqx +1 && y== sqy + 1)){
                                            System.out.println("Trovata due");
                                            return true;



                                        }
                                    }
                                }


                            }
                        }

                    }
                }

            }
        }
        return false;
    }

    */
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] shelfGrid = shelf.getGrid();
        boolean trovato1 = false;
        boolean trovato2 = false;
        int sqx=0;
        int sqy=0;
        outerLoop:
        for (int col = 0; col < 4; col++) {
            for (int raw = 0; raw < 5; raw++) {
                if (shelfGrid[raw][col] != null && shelfGrid[raw + 1][col + 1] != null && shelfGrid[raw][col + 1] != null && shelfGrid[raw + 1][col] != null) {
                    if (shelfGrid[raw][col].getType() == shelfGrid[raw + 1][col + 1].getType() && shelfGrid[raw][col].getType() == shelfGrid[raw][col + 1].getType() && shelfGrid[raw][col].getType() == shelfGrid[raw + 1][col].getType()) {
                        sqx = raw;
                        sqy = col;
                        System.out.println(sqx);
                        System.out.println(sqy);
                        trovato1 = true;
                        break outerLoop;
                    }
                }
            }
        }
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 5; x++) {
                if (shelfGrid[x][y] != null && shelfGrid[x + 1][y + 1] != null && shelfGrid[x][y + 1] != null && shelfGrid[x + 1][y] != null) {
                    if (shelfGrid[x][y].getType() == shelfGrid[x + 1][y + 1].getType() && shelfGrid[x][y].getType() == shelfGrid[x][y + 1].getType() && shelfGrid[x][y].getType() == shelfGrid[x + 1][y].getType()) {
                        if (!(x == sqx && y == sqy) && !(x == sqx && y == sqy + 1) && !(x == sqx + 1 && y == sqy) && !(x == sqx + 1 && y == sqy + 1)) {
                            trovato2 = true;
                        }
                    }
                }
            }
        }
        return trovato2 && trovato1;
    }




    @Override
    public String toString(){
        return "Two groups each containing 4 tiles of the same type in a 2x2 square. The tiles of one square can be different from those of the other square.";
    }

    public int getClassID(){
        return 8;
    }
}
