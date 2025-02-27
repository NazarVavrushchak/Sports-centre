package sports.center.com.exception.exceptions;

public class EmptyTrainerListException extends RuntimeException{
    public EmptyTrainerListException(String message){
        super(message);
    }
}