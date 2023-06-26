package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.*;

import java.io.Serializable;

public interface StrategyCommonGoal  {
    public boolean executeStrategy(Shelf shelf);

    @Override
    public String toString();

    public int getClassID();
}
