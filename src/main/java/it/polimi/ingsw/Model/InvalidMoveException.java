package main.java.it.polimi.ingsw.Model;

public class InvalidMoveException extends Exception {
    public InvalidMoveException(String errorMessage){
        super(errorMessage);
    }
}
