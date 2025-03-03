package sports.center.com.exception.exceptions;

public class TrainingTypeNotFoundException extends RuntimeException{
    public TrainingTypeNotFoundException(String message){
        super(message);
    }
}