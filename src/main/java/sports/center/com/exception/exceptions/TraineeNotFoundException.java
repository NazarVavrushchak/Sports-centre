package sports.center.com.exception.exceptions;

public class TraineeNotFoundException extends RuntimeException {
    public TraineeNotFoundException(String message) {
        super(message);
    }
}