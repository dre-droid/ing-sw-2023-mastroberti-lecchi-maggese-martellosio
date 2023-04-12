package main.java.it.polimi.ingsw.Model.CommonGoalCardStuff;

import main.java.it.polimi.ingsw.Model.*;

public interface StrategyCommonGoal {
    public boolean executeStrategy(Shelf shelf);

    @Override
    public String toString();
}
