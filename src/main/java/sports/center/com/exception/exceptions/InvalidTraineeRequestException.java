package sports.center.com.exception.exceptions;

import jakarta.validation.ConstraintViolation;
import java.util.Set;

public class InvalidTraineeRequestException extends BaseValidationException {
    public InvalidTraineeRequestException(String message, Set<ConstraintViolation<?>> violations) {
        super(message, violations);
    }
}