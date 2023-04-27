package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;


import main.java.it.polimi.ingsw.Model.*;

import java.util.ArrayList;
import java.util.List;

public class FourGroupsOfAtLeastFourSameTypeTiles implements StrategyCommonGoal{

    class Coordinate{
        public int x;
        public int y;
        public Coordinate(int x, int y){this.x = x;this.y =y;}
    }
    @Override
    public boolean executeStrategy(Shelf shelf) {
        Tile[][] grid = shelf.getGrid();


        List<Coordinate> alreadyCheckedPositions = new ArrayList<Coordinate>();
        List<Coordinate> positionsToBeChecked = new ArrayList<Coordinate>();
        int groupCounter=0;
        int tilesInGroup=0;
        Coordinate coord;
        boolean alreadyChecked;

        for(int row =0;row<6;row++){
            for(int col = 0;col<5;col++){
                if(grid[row][col]!=null){
                    alreadyChecked = alreadyChecked(row,col,alreadyCheckedPositions);
                    //System.out.println("cella: ("+row+","+col+")");
                    if(!alreadyChecked){
                        positionsToBeChecked.add(new Coordinate(row,col));

                        do{
                            coord = positionsToBeChecked.remove(positionsToBeChecked.size()-1);
                            alreadyCheckedPositions.add(coord);
                            //System.out.println("---cella: ("+coord.x+","+coord.y+")");
                            //up
                            if(coord.x-1>0){
                                if(grid[coord.x-1][coord.y]!=null && !alreadyChecked(coord.x-1,coord.y,alreadyCheckedPositions) && !alreadyChecked(coord.x-1,coord.y,positionsToBeChecked)){
                                    if(grid[coord.x-1][coord.y].getType() == grid[coord.x][coord.y].getType()){
                                        positionsToBeChecked.add(new Coordinate(coord.x-1,col));
                                        //System.out.println("UP");
                                    }

                                }
                            }
                            //down
                            if(coord.x+1<6){
                                if(grid[coord.x+1][coord.y]!=null && !alreadyChecked(coord.x+1,coord.y,alreadyCheckedPositions) && !alreadyChecked(coord.x+1,coord.y,positionsToBeChecked)){
                                    if(grid[coord.x+1][coord.y].getType() == grid[coord.x][coord.y].getType()){
                                        positionsToBeChecked.add(new Coordinate(coord.x+1,coord.y));
                                        //System.out.println("DOWN");
                                    }

                                }
                            }
                            //left
                            if(coord.y-1>0){
                                if(grid[coord.x][coord.y-1]!=null && !alreadyChecked(coord.x,coord.y-1,alreadyCheckedPositions) && !alreadyChecked(coord.x,coord.y-1,positionsToBeChecked)){
                                    if(grid[coord.x][coord.y-1].getType() == grid[coord.x][coord.y].getType()){
                                        //System.out.println("LEFT");
                                        positionsToBeChecked.add(new Coordinate(coord.x,coord.y-1));
                                    }
                                }
                            }
                            //right
                            if(coord.y+1<5){
                                if(grid[coord.x][coord.y+1]!=null && !alreadyChecked(coord.x,coord.y+1,alreadyCheckedPositions) && !alreadyChecked(coord.x,coord.y+1,positionsToBeChecked)){
                                    if(grid[coord.x][coord.y+1].getType() == grid[coord.x][coord.y].getType()){
                                        //System.out.println("RIGHT");
                                        positionsToBeChecked.add(new Coordinate(coord.x,coord.y+1));
                                    }

                                }
                            }
                            tilesInGroup+=1;

                        }while(positionsToBeChecked.size()!=0);
                        if(tilesInGroup>=4)
                            groupCounter+=1;
                        tilesInGroup=0;
                        //System.out.println("{");
                        //for(Coordinate c: alreadyCheckedPositions){
                         //   System.out.print("("+c.x+","+c.y+") ");
                        //}
                        //System.out.println("}");
                        //System.out.println("Gruppi trovati finora:"+groupCounter);

                    }
                }

            }
        }
        return groupCounter >= 4;
    }

    private boolean alreadyChecked(int x, int y, List<Coordinate> alreadyChecked){
        boolean flag = false;
        Coordinate coord;
        for(int i=0;i<alreadyChecked.size();i++){
            coord = alreadyChecked.get(i);
            if(x == coord.x && y == coord.y)
                flag=true;
        }
        return flag;
    }

    @Override
    public String toString(){
        return "Four groups each containing at least 4 tiles of the same type (not necessarily in the depicted shape). The tiles of one group can be different from those of another group.";
    }

    public int getClassID(){
        return 4;
    }

}
