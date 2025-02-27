package sports.center.com.exception.exceptions;

public class TraineeNotFoundException extends RuntimeException {
    public TraineeNotFoundException(String username) {
        super("Trainee not found: " + username);
    }
}